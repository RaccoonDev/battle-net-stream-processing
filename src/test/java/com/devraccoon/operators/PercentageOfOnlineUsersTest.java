package com.devraccoon.operators;

import com.devraccoon.models.PlayerEvent;
import com.devraccoon.models.PlayerEventType;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class PercentageOfOnlineUsersTest {

    private final Tuple3[] testCases = new Tuple3[]{
            Tuple3.of(new PlayerEvent(Instant.now(), UUID.randomUUID(), PlayerEventType.REGISTERED), 1, Tuple2.of(1L, 0L)),
            Tuple3.of(new PlayerEvent(Instant.now(), UUID.randomUUID(), PlayerEventType.REGISTERED), 1, Tuple2.of(2L, 0L)),
            Tuple3.of(new PlayerEvent(Instant.now(), UUID.randomUUID(), PlayerEventType.ONLINE), 1, Tuple2.of(2L, 1L)),
            Tuple3.of(new PlayerEvent(Instant.now(), UUID.randomUUID(), PlayerEventType.ONLINE), 1, Tuple2.of(2L, 2L)),
            Tuple3.of(new PlayerEvent(Instant.now(), UUID.randomUUID(), PlayerEventType.OFFLINE), 2, Tuple2.of(2L, 1L)),
    };

    @Test
    public void registrationsCounterIncreases() throws Exception {
        PercentageOfOnlineUsers processFunction = new PercentageOfOnlineUsers();
        Collector<Tuple2<Long, Long>> collector = mock(Collector.class);

        for(Tuple3<PlayerEvent, Integer, Tuple2<Long, Long>> t : testCases) {
            processFunction.processElement(t.f0, null, collector);
            Mockito.verify(collector, times(t.f1)).collect(t.f2);
        }
    }



}