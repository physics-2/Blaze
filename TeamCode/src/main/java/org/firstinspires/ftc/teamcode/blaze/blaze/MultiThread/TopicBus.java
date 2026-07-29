package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TopicBus {
    private final static Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    private final static Map<String, AtomicReference<Object>> latest = new ConcurrentHashMap<>();

    private static final List<BiConsumer<String, Object>> wildcardSubscribers = new CopyOnWriteArrayList<>();


    public static void publish(String topicName, Object message) {
        latest.computeIfAbsent(topicName, k -> new AtomicReference<>()).set(message);

        for (BiConsumer<String, Object> subscriber : wildcardSubscribers) {
            try {
                subscriber.accept(topicName, message);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        List<Consumer<Object>> list = subscribers.get(topicName);
        if (list == null) return;

        for (Consumer<Object> consumer : list) {
            try {
                consumer.accept(message);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void subscribe(String topicName, Consumer<Object> consumer) {
        subscribers.computeIfAbsent(topicName, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    public static Object getLatest(String topicName) {
        AtomicReference<Object> ref = latest.get(topicName);
        return ref != null ? ref.get() : null;
    }


    @SuppressWarnings("unchecked")
    public static  <T> T getLatest(String topicName, Class<T> type, T defaultValue) {
        Object value = getLatest(topicName);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    public static  <T> void publish(Topic<T> topic, T message) {
        publish(topic.name, message);
    }

    public static  <T> void subscribe(Topic<T> topic, Consumer<T> consumer) {
        subscribers.computeIfAbsent(topic.name, k -> new CopyOnWriteArrayList<>())
                .add(message -> {
                    if (topic.type.isInstance(message)) {
                        consumer.accept(topic.type.cast(message));
                    }
                });
    }

    public static  <T> T getLatest(Topic<T> topic, T defaultValue) {
        return getLatest(topic.name, topic.type, defaultValue);
    }



    public static void subscribeAll(BiConsumer<String, Object> consumer) {
        wildcardSubscribers.add(consumer);
    }



    public void clear() {
        subscribers.clear();
        latest.clear();
        wildcardSubscribers.clear();
    }
}