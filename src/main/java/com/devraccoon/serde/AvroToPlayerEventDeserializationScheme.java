package com.devraccoon.serde;

import com.devraccoon.models.*;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class AvroToPlayerEventDeserializationScheme implements KafkaRecordDeserializationSchema<PlayerEvent> {

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

            switch (maybeEventType.get()) {
                case REGISTERED:
                    collector.collect(new PlayerEventRegistered(eventTime, maybePlayerId.get()));
                    break;
                case ONLINE:
                    collector.collect(new PlayerEventOnline(eventTime, maybePlayerId.get()));
                    break;
                case OFFLINE:
                    collector.collect(new PlayerEventOffline(eventTime, maybePlayerId.get()));
                    break;
                case LOOKING_FOR_A_GAME:
                    getEventType(r.get("gameType").toString()).ifPresent(gameType ->
                            collector.collect(
                                    new PlayerEventLookingForGame(eventTime, maybePlayerId.get(), gameType)
                            )
                    );
                    break;
            }
        }
    }

    private Optional<PlayerEventType> mapEventType(String className) {
        switch (className) {
            case "PlayerRegistered":
                return Optional.of(PlayerEventType.REGISTERED);
            case "PlayerOnline":
                return Optional.of(PlayerEventType.ONLINE);
            case "PlayerOffline":
                return Optional.of(PlayerEventType.OFFLINE);
            case "PlayerIsLookingForAGame":
                return Optional.of(PlayerEventType.LOOKING_FOR_A_GAME);
            default:
                return Optional.empty();
        }
    }

    private Optional<GameType> getEventType(String eventType) {
        switch (eventType) {
            case "FourVsFour":
                return Optional.of(GameType.FOUR_VS_FOUR);
            case "OneVsOne":
                return Optional.of(GameType.ONE_VS_ONE);
            case "ThreeVsThree":
                return Optional.of(GameType.THREE_VS_THREE);
            case "TwoVsTwo":
                return Optional.of(GameType.TWO_VS_TWO);
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
