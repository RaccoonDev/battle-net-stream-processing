/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devraccoon;

import com.devraccoon.models.PlayerOffline;
import com.devraccoon.models.PlayerOnline;
import com.devraccoon.models.PlayerRegistered;
import com.devraccoon.models.ServerEvent;
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
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class BattleNetEventsProcessor {

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final String topic = "battlenet.server.events.v1";
        final String groupId = "battle-net-events-processor-1";
        final String schemaRegistryUrl = "http://localhost:8081";

        KafkaSource<ServerEvent> source = KafkaSource.<ServerEvent>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new KafkaGenericAvroDeserializationScheme(schemaRegistryUrl, topic))
                .build();

        WatermarkStrategy<ServerEvent> watermarkStrategy = WatermarkStrategy
                .<ServerEvent>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                .withTimestampAssigner(new PlayerOnlineTimestampAssigner())
                .withIdleness(Duration.ofSeconds(1));

        DataStream<ServerEvent> dataStream = env.fromSource(
                source,
                watermarkStrategy,
                "playerOnlineEvents"
        );

        dataStream.filter(s -> s.maybeGetPlayerId().isPresent())
                .keyBy(s -> s.maybeGetPlayerId().get())
                .window(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)))
                .apply(new WindowFunction<ServerEvent, Tuple3<UUID, Long, String>, UUID, TimeWindow>() {
                    @Override
                    public void apply(UUID uuid, TimeWindow window, Iterable<ServerEvent> input, Collector<Tuple3<UUID, Long, String>> out) throws Exception {
                        long loginsPerUserPerWindow = StreamSupport.stream(input.spliterator(), false).filter(s -> s instanceof PlayerOnline).count();
                        String message = String.format("Window: %s - %s; User id: %s; logins count: %s", window.getStart(), window.getEnd(), uuid, loginsPerUserPerWindow);
                        out.collect(Tuple3.of(uuid, loginsPerUserPerWindow, message));
                    }
                })
                .print();

        // execute program
        env.execute("Flink Streaming Java API Skeleton");
    }
}

class PlayerOnlineTimestampAssigner implements SerializableTimestampAssigner<ServerEvent> {
    @Override
    public long extractTimestamp(ServerEvent element, long recordTimestamp) {
        return element.getEventTime().toEpochMilli();
    }
}

class KafkaGenericAvroDeserializationScheme implements KafkaRecordDeserializationSchema<ServerEvent> {
    transient private KafkaAvroDeserializer deserializer;
    private final String topic;
    private final String schemaRegistryUrl;

    public KafkaGenericAvroDeserializationScheme(String schemaRegistryUrl, String topic) {
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.topic = topic;
    }

    @Override
    public void open(DeserializationSchema.InitializationContext context) {
        Properties props = new Properties();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);

        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
                schemaRegistryUrl,
                AbstractKafkaAvroSerDeConfig.MAX_SCHEMAS_PER_SUBJECT_DEFAULT);

        this.deserializer = new KafkaAvroDeserializer(schemaRegistryClient, typeCastConvert(props));
    }

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<ServerEvent> out) throws IOException {
        GenericRecord r = (GenericRecord) deserializer.deserialize(topic, record.value());
        String className = r.getSchema().getName();

        switch (className) {
            case "PlayerRegistered":
                out.collect(new PlayerRegistered(
                        Instant.ofEpochMilli((long) r.get("eventTime")),
                        java.util.UUID.fromString(r.get("playerId").toString()),
                        r.get("nickname").toString()));
                break;
            case "PlayerOnline":
                out.collect(new PlayerOnline(Instant.ofEpochMilli((long) r.get("eventTime")),
                        java.util.UUID.fromString(r.get("playerId").toString()),
                        r.get("nickname").toString()));
                break;
            case "PlayerOffline":
                out.collect(new PlayerOffline(Instant.ofEpochMilli((long) r.get("eventTime")),
                        java.util.UUID.fromString(r.get("playerId").toString()),
                        r.get("nickname").toString()));
                break;
        }
    }

    @Override
    public TypeInformation<ServerEvent> getProducedType() {
        return TypeExtractor.getForClass(ServerEvent.class);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, ?> typeCastConvert(Properties prop) {
        Map step1 = prop;
        Map<String, ?> step2 = (Map<String, ?>) step1;
        return new HashMap<>(step2);
    }
}