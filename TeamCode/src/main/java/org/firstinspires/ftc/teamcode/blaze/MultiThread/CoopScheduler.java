package org.firstinspires.ftc.teamcode.blaze.MultiThread;

import org.firstinspires.ftc.teamcode.blaze.BlazeLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class CoopScheduler {
    private static class Task {
        final String id;
        final long periodMs;
        final Runnable action;

        long nextRunMs;
        boolean enabled = true;

        Task(String id, long periodMs, Runnable action, long nowMs) {
            this.id = id;
            this.periodMs = periodMs;
            this.action = action;
            this.nextRunMs = nowMs + periodMs;
        }
    }

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final Map<String, Task> byId = new ConcurrentHashMap<>();

    private final long warnThresholdMs;

    public CoopScheduler() {
        this(5);
    }

    public CoopScheduler(long warnThresholdMs) {
        this.warnThresholdMs = warnThresholdMs;
    }

    public void addOrReplace(String id, long periodMs, Runnable action) {
        long now = System.currentTimeMillis();

        Task newTask = new Task(id, periodMs, action, now);
        Task old = byId.put(id, newTask);

        if (old != null) {
            old.enabled = false;
        }

        tasks.add(newTask);
        tasks.removeIf(t -> !t.enabled);
    }

    public void remove(String id) {
        Task task = byId.remove(id);

        if (task != null) {
            task.enabled = false;
        }

        tasks.removeIf(t -> !t.enabled);
    }

    public void update(long nowMs) {
        for (Task task : tasks) {
            if (!task.enabled) continue;

            if (nowMs >= task.nextRunMs) {
                long start = System.nanoTime();

                task.action.run();


                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

                if (elapsedMs > warnThresholdMs) {
                    BlazeLogger.addWarnLog ("",
                            "Coop task slow: " + task.id +
                                    " took " + elapsedMs + "ms"
                    );
                }

                long finishedMs = System.currentTimeMillis();

                task.nextRunMs = finishedMs + Math.max(1, task.periodMs);
            }
        }
    }

    public void clear() {
        tasks.clear();
        byId.clear();
    }
}