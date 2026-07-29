package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {
    private final ExecutorManager executors;

    private final Map<String, AtomicBoolean> running = new ConcurrentHashMap<>();
    private final Set<String> executedOnce = ConcurrentHashMap.newKeySet();
    private final Map<String, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    public Scheduler(ExecutorManager executors) {
        this.executors = executors;
    }

    public void run(String id, PoolType pool, ExecutionMode mode, Runnable task) {
        switch (mode) {
            case ONCE:
                if (executedOnce.add(id)) {
                    executors.get(pool).submit(safe(task));
                }
                break;

            case NON_OVERLAPPING:
                AtomicBoolean flag = running.computeIfAbsent(id, k -> new AtomicBoolean(false));

                if (flag.compareAndSet(false, true)) {
                    executors.get(pool).submit(() -> {
                            task.run();
                            flag.set(false);
                    });
                }
                break;

            case ALWAYS:
            default:
                executors.get(pool).submit(safe(task));
                break;
        }
    }

    public void runAlways(String id, PoolType pool, Runnable task) {
        run(id, pool, ExecutionMode.ALWAYS, task);
    }

    public void runNonOverlapping(String id, PoolType pool, Runnable task) {
        run(id, pool, ExecutionMode.NON_OVERLAPPING, task);
    }

    public void runOnce(String id, PoolType pool, Runnable task) {
        run(id, pool, ExecutionMode.ONCE, task);
    }

    public void scheduleAtFixedRate(String id, long periodMs, Runnable task) {
        ScheduledExecutorService executor =
                (ScheduledExecutorService) executors.get(PoolType.SCHEDULED);

        ScheduledFuture<?> old = scheduled.get(id);
        if (old != null) {
            old.cancel(false);
        }

        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(
                safe(task),
                0,
                periodMs,
                TimeUnit.MILLISECONDS
        );

        scheduled.put(id, future);
    }

    public void cancel(String id) {
        ScheduledFuture<?> future = scheduled.remove(id);
        if (future != null) {
            future.cancel(false);
        }

        executedOnce.remove(id);
        running.remove(id);
    }

    public void cancelAll() {
        for (ScheduledFuture<?> future : scheduled.values()) {
            future.cancel(false);
        }

        scheduled.clear();
        executedOnce.clear();
        running.clear();
    }

    private Runnable safe(Runnable task) {
        return task::run;
    }
}