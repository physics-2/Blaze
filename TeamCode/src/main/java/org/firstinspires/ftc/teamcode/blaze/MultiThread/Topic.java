package org.firstinspires.ftc.teamcode.blaze.MultiThread;

public final class Topic<T> {
    public final String name;
    public final Class<T> type;

    private Topic(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public static <T> Topic<T> of(String name, Class<T> type) {
        return new Topic<>(name, type);
    }
}