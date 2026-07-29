package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

import org.firstinspires.ftc.teamcode.blaze.blaze.BlazeLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class SyncTopicBus {
    private static final Map<String, List<Consumer<Object>>> subscribers = new HashMap<>();
    private static final Map<String, Object> latest = new HashMap<>();

    SyncTopicBus(){}

    public static void publish(String topicName, Object message) {


        latest.put(topicName, message);

        List<Consumer<Object>> consumers = subscribers.get(topicName);
        if (consumers == null) return;

        for (int i = 0; i < consumers.size(); i++) {
            consumers.get(i).accept(message);
        }
    }

    public static void subscribe(String topicName, Consumer<Object> consumer) {
        BlazeLogger.addDefaultLog("SyncTopicBus","Got subscribe " + consumer.getClass().getSimpleName() + " to topic " + topicName);
        subscribers.computeIfAbsent(topicName, k -> new ArrayList<>()).add(consumer);
    }

    public static Object getLatest(String topicName) {
        return latest.get(topicName);
    }

    @SuppressWarnings("unchecked")
    public static  <T> T getLatest(String topicName, Class<T> type, T defaultValue) {
        Object value = latest.get(topicName);
        if (value != null && type.isInstance(value)) return (T) value;
        return defaultValue;
    }

    public void clear() {
        subscribers.clear();
        latest.clear();
    }
}