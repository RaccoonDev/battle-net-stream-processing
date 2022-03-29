package com.devraccoon.operators;

import com.devraccoon.models.PlayerEvent;
import com.devraccoon.models.PlayerEventOffline;
import com.devraccoon.models.PlayerEventOnline;
import com.devraccoon.models.PlayerEventRegistered;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PercentageOfOnlineUsers extends ProcessFunction<PlayerEvent, Tuple2<Long, Long>> {

    private static Logger logger = LoggerFactory.getLogger(PercentageOfOnlineUsers.class);

    transient long registrations;
    transient long online;

    @Override
    public void processElement(PlayerEvent value, ProcessFunction<PlayerEvent, Tuple2<Long, Long>>.Context ctx, Collector<Tuple2<Long, Long>> out) throws Exception {

        if (value instanceof PlayerEventRegistered)
            registrations++;
        else if (value instanceof PlayerEventOnline)
            online++;
        else if (value instanceof PlayerEventOffline)
            online--;


        Tuple2<Long, Long> t = Tuple2.of(registrations, online);
        out.collect(t);
        logger.info(String.format("Registrations: %d, online: %d; Rate: %.2f", t.f0, t.f1, ((double) t.f1 / (double) t.f0) * 100.0));
    }
}
