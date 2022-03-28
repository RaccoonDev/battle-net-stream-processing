package com.devraccoon;

import com.devraccoon.models.PlayerEvent;
import com.devraccoon.models.PlayerEventType;
import com.devraccoon.operators.PercentageOfOnlineUsers;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
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
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

/*
  --schema-registry-url http://schema-registry:8081 --bootstrap-servers broker:29092
 */
public class BattleNetStreamProcessingJob {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                        5,
                        org.apache.flink.api.common.time.Time.of(1, TimeUnit.MINUTES))
        );

        env.enableCheckpointing(Duration.ofSeconds(30).toMillis());
        env.getCheckpointConfig().setCheckpointStorage("s3://testme/checkpoints");
        env.getCheckpointConfig().setCheckpointTimeout(Duration.ofSeconds(60).toMillis());
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(3);
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
        );

        ParameterTool params = ParameterTool.fromArgs(args);

        final String topic = "battlenet.server.events.v1";
        final String schemaRegistryUrl = params.getRequired("schema-registry-url");// "http://localhost:8081";
        final String bootstrapServers = params.getRequired("bootstrap-servers"); // "localhost:9092";
        final String groupId = "battlenet-events-processor-1";

        KafkaSource<PlayerEvent> source = KafkaSource.<PlayerEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new AvroToPlayerEventDeserializationScheme(topic, schemaRegistryUrl))
                .build();

        WatermarkStrategy<PlayerEvent> watermarkStrategy = WatermarkStrategy
                .<PlayerEvent>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                .withTimestampAssigner(new SerializableTimestampAssigner<PlayerEvent>() {
                    @Override
                    public long extractTimestamp(PlayerEvent playerEvent, long l) {
                        return playerEvent.getEventTime().toEpochMilli();
                    }
                })
                .withIdleness(Duration.ofSeconds(1));
        DataStreamSource<PlayerEvent> playerEventsStream = env.fromSource(source, watermarkStrategy, "playerEvents");
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
                                    if (lastEvent.getEventType() == PlayerEventType.ONLINE) {
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

class AvroToPlayerEventDeserializationScheme implements KafkaRecordDeserializationSchema<PlayerEvent> {

    transient private KafkaAvroDeserializer deserializer;
    private final String topic;
    private final String schemaRegistryUrl;

    public AvroToPlayerEventDeserializationScheme(String topic, String schemaRegistryUrl) {
        this.topic = topic;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public void open(DeserializationSchema.InitializationContext context) throws Exception {

        Properties props = new Properties();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);

        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl,
                AbstractKafkaAvroSerDeConfig.MAX_SCHEMAS_PER_SUBJECT_DEFAULT
        );

        this.deserializer = new KafkaAvroDeserializer(schemaRegistryClient, typeCastConvert(props));
    }

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> consumerRecord, Collector<PlayerEvent> collector) throws IOException {
        GenericRecord r = (GenericRecord) deserializer.deserialize(topic, consumerRecord.value());
        String className = r.getSchema().getName();

        Instant eventTime = Instant.ofEpochMilli((long) r.get("eventTime"));
        Optional<PlayerEventType> maybeEventType = mapEventType(className);
        Optional<UUID> maybePlayerId = Optional.ofNullable(r.get("playerId")).map(Object::toString).map(UUID::fromString);

        if (maybeEventType.isPresent() && maybePlayerId.isPresent()) {
            collector.collect(new PlayerEvent(eventTime, maybePlayerId.get(), maybeEventType.get()));
        }
        ;

    }

    private Optional<PlayerEventType> mapEventType(String className) {
        switch (className) {
            case "PlayerRegistered":
                return Optional.of(PlayerEventType.REGISTERED);
            case "PlayerOnline":
                return Optional.of(PlayerEventType.ONLINE);
            case "PlayerOffline":
                return Optional.of(PlayerEventType.OFFLINE);
            default:
                return Optional.empty();
        }
    }


    @Override
    public TypeInformation<PlayerEvent> getProducedType() {
        return TypeExtractor.getForClass(PlayerEvent.class);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, ?> typeCastConvert(Properties prop) {
        Map step1 = prop;
        Map<String, ?> step2 = (Map<String, ?>) step1;
        return new HashMap<>(step2);
    }
}
