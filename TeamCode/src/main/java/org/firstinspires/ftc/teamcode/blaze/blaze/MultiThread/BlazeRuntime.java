package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

public final class BlazeRuntime {
    private static volatile TopicBus bus = new TopicBus();
    private static volatile SyncTopicBus fastBus = new SyncTopicBus();
    private static volatile ExecutorManager executors = new ExecutorManager();
    private static volatile Scheduler scheduler = new Scheduler(executors);

    public BlazeRuntime() {

    }

    public static void create(){
        if(bus == null){
            bus = new TopicBus();
        }
        if(executors == null){
            executors = new ExecutorManager();
        }
        if(scheduler == null){
            scheduler = new Scheduler(executors);
        }
        if(fastBus == null){
            fastBus = new SyncTopicBus();
        }
    }

    public static TopicBus bus() {
        return bus;
    }

    public static Scheduler scheduler() {
        return scheduler;
    }

    public static ExecutorManager executors() {
        return executors;
    }
    public static SyncTopicBus fastBus(){
        return fastBus;
    }

    public static synchronized void reset() {
        shutdown();
        create();
    }

    public static synchronized void shutdown() {
        scheduler.cancelAll();
        executors.shutdown();
        fastBus.clear();
        bus.clear();
    }
}