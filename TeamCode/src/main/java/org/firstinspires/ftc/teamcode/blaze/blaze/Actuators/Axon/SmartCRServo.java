package org.firstinspires.ftc.teamcode.blaze.blaze.Actuators.Axon;

import static java.lang.Math.abs;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.blaze.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.blaze.BlazeLogger;
import org.firstinspires.ftc.teamcode.blaze.blaze.Controllers.Controller;
import org.firstinspires.ftc.teamcode.blaze.blaze.Controllers.PIDController;

/**
 * An extended wrapper class for CRServos with more features
 * such as integration with absolute analog encoders for Axon servos
 * and their absolute encoders.
 *
 * @author Saket(team 23511) & physics
 */
@Configurable
public class SmartCRServo implements Actuator {
    String id;
    private final Encoder encoder;
    boolean isRawSpeed = false;
    private final PIDCoefficients CRCoefficients = new PIDCoefficients(0.0067,0.0063,0.00033);

    public Controller PID = new PIDController(CRCoefficients);

    public double tolerance = 10;

    private double target;
    CRServoCustom servo;
    public SmartCRServo(HardwareMap hwMap, String id, String encoderID, AngleUnit angleUnit) {
        long startTime = System.currentTimeMillis();
        servo = new CRServoCustom(hwMap,id);
        this.id = id;
        this.encoder = new AnalogEncoder(hwMap, encoderID, 3.3, angleUnit);
        servo.setPower(0);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartCRServo." + servo.id(),"Took " + timeToDo + " ms to full init");
    }

    private SmartCRServo(CRServoCustom servo, Encoder encoder, Controller controller, double tolerance) {
        long startTime = System.currentTimeMillis();
        this.servo = servo;
        this.tolerance = tolerance;
        PID = controller;
        this.encoder = encoder;
        this.id = servo.id();
        servo.setPower(0);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartCRServo." + servo.id(),"Took " + timeToDo + " ms to full init");
    }

    public SmartCRServo(HardwareMap hwMap, String id, String encoderID, Controller controller) {
        long startTime = System.currentTimeMillis();
        servo = new CRServoCustom(hwMap,id);
        this.encoder = new AnalogEncoder(hwMap, encoderID, 3.3, AngleUnit.DEGREES);
        PID = controller;
        this.id = servo.id();
        servo.setPower(0);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartCRServo." + servo.id(),"Took " + timeToDo + " ms to full init");
    }

    @Override
    public void setPower(double power) {
        isRawSpeed = true;
        servo.setPower(power);
    }

    @Override
    public void setTarget(double target) {
        isRawSpeed = false;
        this.target = target;
    }

    @Override
    public void setController(Controller controller) {
        PID = controller;
    }

    @Override
    public Controller getController() {
        return PID;
    }

    @Override
    public double getPosition() {
        return encoder.getPosition();
    }

    @Override
    public boolean isReady() {
        return abs(target - encoder.getPosition()) < tolerance;
    }

    @Override
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public void reset() {
        setTarget(0);
    }

    @Override
    public String getID() {
        return id;
    }

    public double getPower(){
        return servo.getPower();
    }

    public void update(){
        if(!isRawSpeed){
            servo.setPower(PID.calculate(normalizeAngle(target - encoder.getPosition(),false,AngleUnit.DEGREES)));
        }
    }


    public static double returnMaxForAngleUnit(AngleUnit angleUnit) {
        if (angleUnit.equals(AngleUnit.RADIANS)) {
            return Math.PI * 2;
        } else {
            return 360;
        }
    }
    public static double normalizeAngle(double angle, boolean zeroToMax, AngleUnit angleUnit) {
        double max = returnMaxForAngleUnit(angleUnit);
        double angle2 = angle % max;
        if (zeroToMax && angle2 < 0) {
            return angle2 + max;
        } else if (!zeroToMax) {
            if (angle2 > max/2) {
                return angle2 - max;
            } else if (angle2 < -max/2) {
                return angle2 + max;
            }
        }
        return angle2;
    }

    @Override
    public String toString() {
        return "SmartCRServo{" +
                "CRCoefficients=" + CRCoefficients +
                ", Power=" + PID.getControl() +
                '}';
    }

    public static class builder{
        CRServoCustom servo;
        Encoder encoder;
        public double tolerance = 10;
        Controller controller;
        HardwareMap hardwareMap;
        String encoderName;
        double encoderVoltage = 3.3;
        AngleUnit angleUnit = AngleUnit.DEGREES;

        public builder(HardwareMap hardwareMap,String name){
            this.hardwareMap = hardwareMap;
            servo = new CRServoCustom(hardwareMap, name);
        }

        public builder controller(Controller controller) {
            this.controller = controller;
            return this;
        }


        public builder tolerance(double tolerance) {
            this.tolerance = tolerance;
            return this;
        }


        public builder addAnalogEncoder(String name, double voltage, AngleUnit angleUnit) {
            this.encoderName = name;
            this.encoderVoltage = voltage;
            this.angleUnit = angleUnit;
            return this;
        }


        public builder copy(SmartCRServo servo){
            this.servo = servo.servo;
            this.encoder = servo.encoder;
            this.tolerance = servo.tolerance;

            return this;
        }

        public SmartCRServo build(){
            if (encoder == null && encoderName != null) {
                encoder = new AnalogEncoder(hardwareMap, encoderName, encoderVoltage, angleUnit);
            }
            return new SmartCRServo(servo,encoder,controller,tolerance);
        }
    }
}