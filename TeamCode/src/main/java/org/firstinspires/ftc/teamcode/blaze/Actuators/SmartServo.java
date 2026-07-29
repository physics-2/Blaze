package org.firstinspires.ftc.teamcode.blaze.Actuators;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.blaze.BlazeLogger;

public class SmartServo implements Actuator{
    Servo servo;
    double tolerance;

    private double max = 1;
    private double min = 0;
    private  double minRaw = 0;
    private  double maxRaw = 1;
    String name;


    public SmartServo(Servo servo){
        this.servo = servo;
    }

    public SmartServo(HardwareMap hardwareMap,String name, Servo.Direction direction){
        long startTime = System.currentTimeMillis();
        servo = hardwareMap.get(Servo.class,name);
        this.name = name;
        servo.setDirection(direction);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartServo." + name,"Took " + timeToDo + " ms to full init");
    }

    @Override
    public void setTarget(double target) {
        if (target < min) target = min;
        if (target > max) target = max;


        double position = minRaw +
                (maxRaw - minRaw) *
                        (target - min) /
                        (max - min);

        servo.setPosition(position);
    }

    @Override
    public double getPosition() {
        double raw = servo.getPosition();

        if (maxRaw == minRaw) return min;
        return min + (max - min) * (raw - minRaw) / (maxRaw - minRaw);
    }


    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public void reset() {}


    @Override
    public String getID() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return "SmartServo{" +
                "servo=" + servo.getDeviceName() +
                ", tolerance=" + tolerance +
                '}';
    }

    @Override
    public void scale(double from, double fromRaw, double to, double toRaw) {
        min = from;
        minRaw = fromRaw;
        max = to;
        maxRaw = toRaw;
    }
}
