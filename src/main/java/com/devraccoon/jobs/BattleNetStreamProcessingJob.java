package com.devraccoon.jobs;

import com.devraccoon.models.PlayerEvent;
import com.devraccoon.models.PlayerEventOnline;
import com.devraccoon.models.PlayerEventType;
import com.devraccoon.operators.PercentageOfOnlineUsers;
import com.devraccoon.serde.AvroToPlayerEventDeserializationScheme;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

/*
  --schema-registry-url http://schema-registry:8081 --bootstrap-servers broker:29092
 */
public class BattleNetStreamProcessingJob {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<PlayerEvent> playerEventsStream = BattleNetStream
                .getPlayerEventDataStream(args,"battlenet-events-processor-1", env);
        DataStream<String> streamOfThePercentage = getStreamOfThePercentage(playerEventsStream);
        streamOfThePercentage.writeAsText("s3://testme/outout", FileSystem.WriteMode.OVERWRITE);

        DataStream<String> onlineRateMessages = playerEventsStream
                .keyBy(PlayerEvent::getPlayerId)
                .window(SlidingEventTimeWindows.of(Time.seconds(20), Time.seconds(5)))
                .process(new ProcessWindowFunction<PlayerEvent, Integer, UUID, TimeWindow>() {
                    @Override
                    public void process(
                            UUID key,
                            ProcessWindowFunction<PlayerEvent, Integer, UUID, TimeWindow>.Context context,
                            Iterable<PlayerEvent> elements,
                            Collector<Integer> out) throws Exception {
                        StreamSupport
                                .stream(elements.spliterator(), false).reduce(((f, s) -> s))
                                .ifPresent(lastEvent -> {
                                    if (lastEvent instanceof PlayerEventOnline) {
                                        out.collect(1);
                                    }
                                });
                    }
                })
                .windowAll(SlidingEventTimeWindows.of(Time.seconds(20), Time.seconds(5)))
                .apply(new AllWindowFunction<Integer, String, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<Integer> values, Collector<String> out) throws Exception {
                        long count = StreamSupport.stream(values.spliterator(), false).count();
                        String message = String.format("Window: [%s]; Number of online people who are not offline: [%d]", window.toString(), count);
                        out.collect(message);
                    }
                });

        final FileSink<String> fileSink = FileSink
                .forRowFormat(
                        new Path("s3://testme/onlineRateMessages"),
                        new SimpleStringEncoder<String>("UTF-8"))
                .build();

        onlineRateMessages.sinkTo(fileSink);

        env.execute("Battle Net Server Online Players Rate");
    }



    private static DataStream<String> getStreamOfThePercentage(DataStreamSource<PlayerEvent> playerEventsStream) {
        DataStream<String> streamOfThePercentage = playerEventsStream
                .process(new PercentageOfOnlineUsers())
                .map(t -> String.format("Registrations: %d, online: %d; Rate: %.2f", t.f0, t.f1, ((double) t.f1 / (double) t.f0) * 100.0));
        return streamOfThePercentage;
    }
}

