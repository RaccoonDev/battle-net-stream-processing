package com.devraccoon.jobs;

import com.devraccoon.models.GameFound;
import com.devraccoon.models.GameType;
import com.devraccoon.models.PlayerEvent;
import com.devraccoon.models.PlayerEventLookingForGame;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MatchingGamesJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        FileSource<String> mapNamesSource = FileSource.forRecordStreamFormat(new TextLineFormat(), new Path("./data/maps/"))
                .monitorContinuously(Duration.ofSeconds(2))
                .build();

        DataStream<String> mapNames = env.fromSource(mapNamesSource, WatermarkStrategy.noWatermarks(), "map names")
                .map(m -> String.format("Discovered map: %s", m));

        DataStream<PlayerEventLookingForGame> gameRequestsStream = BattleNetStream
                .getPlayerEventDataStream(args, "battlenet-events-processor-2", env, true)
                .flatMap(new FlatMapFunction<PlayerEvent, PlayerEventLookingForGame>() {
                    @Override
                    public void flatMap(PlayerEvent value, Collector<PlayerEventLookingForGame> out) throws Exception {
                        if (value instanceof PlayerEventLookingForGame) {
                            out.collect((PlayerEventLookingForGame) value);
                        }
                    }
                });


        final MapStateDescriptor<String, String> mapsStateDescriptor = new MapStateDescriptor<>(
                "MapNamesBroadcastState",
                BasicTypeInfo.STRING_TYPE_INFO,
                BasicTypeInfo.STRING_TYPE_INFO);

        BroadcastStream<String> mapsBroadcastStream = mapNames.broadcast(mapsStateDescriptor);

        gameRequestsStream.keyBy(e -> e.getGameType().getTotalNumberOfPlayers())
                .connect(mapsBroadcastStream)
                .process(new FindGameMatch())
                .map(gm -> String.format("Game %s found for players: %s on %s map", gm.getGameType(), gm.getPlayerIds(), gm.getMapName()))
                .print();

        env.execute("Matching Games Job");
    }
}

class FindGameMatch extends KeyedBroadcastProcessFunction<Integer, PlayerEventLookingForGame, String, GameFound> {

    private static final Logger logger = LoggerFactory.getLogger(FindGameMatch.class);

    private final ListStateDescriptor<UUID> queueStateDescriptor = new ListStateDescriptor<>(
            "playersQueue", TypeInformation.of(UUID.class)
    );

    private final ValueStateDescriptor<Long> countDescriptor = new ValueStateDescriptor<>("queueLength", BasicTypeInfo.LONG_TYPE_INFO);

    private final MapStateDescriptor<String, String> mapsStateDescriptor = new MapStateDescriptor<>(
            "MapNamesBroadcastState",
            BasicTypeInfo.STRING_TYPE_INFO,
            BasicTypeInfo.STRING_TYPE_INFO);

    private transient ValueState<Long> stateCount;
    private transient ListState<UUID> statePlayersQueue;

    @Override
    public void open(Configuration parameters) throws Exception {
        this.stateCount = getRuntimeContext().getState(countDescriptor);
        this.statePlayersQueue = getRuntimeContext().getListState(queueStateDescriptor);
    }

    @Override
    public void processElement(PlayerEventLookingForGame value, KeyedBroadcastProcessFunction<Integer, PlayerEventLookingForGame, String, GameFound>.ReadOnlyContext ctx, Collector<GameFound> out) throws Exception {

        long currentCount = Optional.ofNullable(stateCount.value()).orElse(0L);

        if ( ctx.getCurrentKey() <= currentCount + 1) {
            Set<UUID> players = Optional.ofNullable(statePlayersQueue.get()).map(pq -> StreamSupport.stream(pq.spliterator(), false).collect(Collectors.toSet()))
                    .orElse(new HashSet<>());
            players.add(value.getPlayerId());

            out.collect(new GameFound(players, GameType.valueOf(ctx.getCurrentKey()).get(), getRandomMap(ctx)));
            statePlayersQueue.clear();
            stateCount.update(0L);
        } else {
            stateCount.update(currentCount + 1);
            statePlayersQueue.add(value.getPlayerId());
        }
    }

    private static final Random rand = new Random();
    private String getRandomMap(KeyedBroadcastProcessFunction<Integer, PlayerEventLookingForGame, String, GameFound>.ReadOnlyContext ctx) throws Exception {
        List<String> maps = StreamSupport.stream(ctx.getBroadcastState(mapsStateDescriptor).immutableEntries().spliterator(), false).map(Map.Entry::getValue).collect(Collectors.toList());
        if(maps.isEmpty()) return "NO MAPS YET";
        else {
            return maps.get(rand.nextInt(maps.size()));
        }
    }

    @Override
    public void processBroadcastElement(String value, KeyedBroadcastProcessFunction<Integer, PlayerEventLookingForGame, String, GameFound>.Context ctx, Collector<GameFound> out) throws Exception {
        ctx.getBroadcastState(mapsStateDescriptor).put(value, value);
    }
}