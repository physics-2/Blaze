package org.firstinspires.ftc.teamcode.blaze;

public class Command {
    public final String name;
    public final Object[] args;

    public Command(String name, Object... args) {
        this.name = name;
        this.args = args;
    }
}