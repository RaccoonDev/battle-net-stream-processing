package com.devraccoon.jobs;

import com.devraccoon.models.PlayerEvent;
import com.devraccoon.serde.AvroToPlayerEventDeserializationScheme;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BattleNetStream {

    public static DataStreamSource<PlayerEvent> getPlayerEventDataStream(
            String[] args,
            String groupId,
            StreamExecutionEnvironment env) {
        return getPlayerEventDataStream(args, groupId, env, false);
    }

    public static DataStreamSource<PlayerEvent> getPlayerEventDataStream(
            String[] args,
            String groupId,
            StreamExecutionEnvironment env,
            boolean disableCheckpointing) {
        env.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                        5,
                        org.apache.flink.api.common.time.Time.of(1, TimeUnit.MINUTES))
        );

        if(!disableCheckpointing) {
            env.enableCheckpointing(Duration.ofSeconds(30).toMillis());
            env.getCheckpointConfig().setCheckpointStorage("s3://testme/checkpoints");
            env.getCheckpointConfig().setCheckpointTimeout(Duration.ofSeconds(60).toMillis());
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(3);
            env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);
            env.getCheckpointConfig().setExternalizedCheckpointCleanup(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
            );
        }

        ParameterTool params = ParameterTool.fromArgs(args);

        final String topic = "battlenet.server.events.v1";
        final String schemaRegistryUrl = Optional.ofNullable(params.get("schema-registry-url")).orElse("http://localhost:8081");
        final String bootstrapServers = Optional.ofNullable(params.get("bootstrap-servers")).orElse("localhost:9092");

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
        return env.fromSource(source, watermarkStrategy, "playerEvents");
    }
}
