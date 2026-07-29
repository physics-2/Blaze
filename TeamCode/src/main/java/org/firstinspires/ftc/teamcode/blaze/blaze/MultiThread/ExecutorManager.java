package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorManager {

    private final Map<PoolType, ExecutorService> pools = new EnumMap<>(PoolType.class);

    public ExecutorManager() {
        pools.put(PoolType.CACHED, Executors.newCachedThreadPool());
        pools.put(PoolType.FIXED, Executors.newFixedThreadPool(4));
        pools.put(PoolType.SINGLE, Executors.newSingleThreadExecutor());
        pools.put(PoolType.SCHEDULED, Executors.newScheduledThreadPool(2));
    }

    public ExecutorService get(PoolType type) {
        return pools.get(type);
    }

    public void shutdown() {
        for (ExecutorService executor : pools.values()) {
            executor.shutdownNow();
        }
    }
}