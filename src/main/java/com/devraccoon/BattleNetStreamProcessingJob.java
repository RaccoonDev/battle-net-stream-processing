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
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class BattleNetStreamProcessingJob {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


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

        DataStream<String> streamOfThePercentage = env.fromSource(source, watermarkStrategy, "playerEvents")
                .process(new PercentageOfOnlineUsers())
                .map(t -> String.format("Registrations: %d, online: %d; Rate: %.2f", t.f0, t.f1, ((double)t.f1 / (double)t.f0) * 100.0));

        streamOfThePercentage.writeAsText("s3://testme/outout");

        env.execute("Battle Net Server Online Players Rate");
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

        if(maybeEventType.isPresent() && maybePlayerId.isPresent()) {
            collector.collect(new PlayerEvent(eventTime, maybePlayerId.get(), maybeEventType.get()));
        };

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
