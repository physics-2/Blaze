package org.firstinspires.ftc.teamcode.blaze.blaze.Actuators;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.blaze.blaze.BlazeLogger;
import org.firstinspires.ftc.teamcode.blaze.blaze.Controllers.Controller;

@Configurable
public class SmartMotor implements Actuator {

    private  double max_power = 1;
    private final String id;
    private TargetMode targetMode = TargetMode.POSITION;
    private final DcMotorEx dcMotor;
    private Controller controller;
    private double ticksToDegrees;
    private double tolerance = 10;
    private double power;
    private boolean isRawPower;
    private boolean isEncoderReversed = false;
    private double target;
    private SmartMotor(DcMotorEx dcMotor, String name, double ticksToDegrees, Controller controller, boolean isEncoderReversed, TargetMode targetMode, double tolerance, double max_power){
        this.ticksToDegrees = ticksToDegrees;
        this.controller = controller;
        this.max_power = max_power;
        this.dcMotor = dcMotor;
        this.isEncoderReversed = isEncoderReversed;
        this.targetMode = targetMode;
        this.tolerance = tolerance;
        this.id = name;
        dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        dcMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        dcMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public TargetMode getTargetMode() {
        return targetMode;
    }

    public DcMotorEx getDcMotor() {
        return dcMotor;
    }

    public double getTicksToDegrees() {
        return ticksToDegrees;
    }

    public double getTolerance() {
        return tolerance;
    }

    public boolean isEncoderReversed() {
        return isEncoderReversed;
    }

    public SmartMotor(HardwareMap hardwareMap, String name, DcMotorSimple.Direction direction,
                      double ticksToDegrees, Controller controller, boolean isEncoderReversed, double max_power){
        long startTime = System.currentTimeMillis();
        dcMotor = hardwareMap.get(DcMotorEx.class,name);
        this.max_power = max_power;
        dcMotor.setDirection(direction);
        this.ticksToDegrees = ticksToDegrees;
        this.controller = controller;
        this.isEncoderReversed = isEncoderReversed;
        this.id = name;
        dcMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        dcMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartMotor." + name,"Took " + timeToDo + " ms to full init");
    }

    public SmartMotor(HardwareMap hardwareMap, String name, DcMotorSimple.Direction direction){
        long startTime = System.currentTimeMillis();
        dcMotor = hardwareMap.get(DcMotorEx.class,name);
        dcMotor.setDirection(direction);
        this.id = name;
        dcMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        dcMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        long timeToDo = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("SmartMotor." + name,"Took " + timeToDo + " ms to full init");
    }

    public void setDirection(DcMotorSimple.Direction direction){
        dcMotor.setDirection(direction);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior direction){
        dcMotor.setZeroPowerBehavior(direction);
    }

    public double getCurrent(CurrentUnit currentUnit){
        return dcMotor.getCurrent(currentUnit);
    }


    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    public void setTicksToDegrees(double ticksToDegrees) {
        this.ticksToDegrees = ticksToDegrees;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void invertEncoder(boolean isEncoderReversed){
        this.isEncoderReversed = isEncoderReversed;
    }

    @Override
    public void setPower(double power) {
        isRawPower = true;
        this.power = power;
        dcMotor.setPower(power);
    }

    @Override
    public void setTarget(double target){
        isRawPower = false;
        this.target = target;
    }


    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public Controller getController() {
        return controller;
    }


    /**
     * Returns the current PID feedback name
     * @return position,or velocity(in Degrees Per Second) if in the VELOCITY mode
     */
    @Override
    public double getPosition() {

        switch (targetMode) {
            case POSITION:
                return dcMotor.getCurrentPosition() * ticksToDegrees * (isEncoderReversed ? -1 : 1);
            case VELOCITY:
                return (dcMotor.getVelocity() * ticksToDegrees * (isEncoderReversed ? -1 : 1));
            case CUSTOM:
                return dcMotor.getCurrentPosition() * (isEncoderReversed ? -1 : 1);
        }
        return 0;
    }

    public double getVelocity(){
        return (dcMotor.getVelocity() * ticksToDegrees * (isEncoderReversed ? -1 : 1)) / 6.0;
    }

    @Override
    public boolean isReady() {
        if(isRawPower) return true;
        return abs(target - getPosition()) < tolerance;
    }

    @Override
    public void reset() {
        dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        controller.reset();
        dcMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public double getPower() {
        return power;
    }

    @Override
    public String getID() {
        return id;
    }

    public void setCoefficients(Object coefficients){
        controller.setCoefficients(coefficients);
    }

    @Override
    public void update() {
        if(!isRawPower && controller != null) {
            power = controller.calculate(target,getPosition());
            if(abs(power) > max_power){
                power = signum(power) * max_power;
            }
            dcMotor.setPower(power);
        } else if(controller == null) {
            dcMotor.setPower(0);
        }
    }

    @Override
    public void setMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    @Override
    public TargetMode getMode() {
        return targetMode;
    }

    @Override
    public String toString() {
        return "SmartMotor{" +
                "power=" + power +
                ", target=" + target +
                '}';
    }

    public static class builder{
        DcMotorEx dcMotor;
        double max_power = 1;
        TargetMode targetMode = TargetMode.POSITION;
        Controller controller;
        double ticksToDegrees;
        String name;
        double tolerance = 10;
        boolean isEncoderReversed = false;
        public builder(HardwareMap hardwareMap,String name){
            this.name = name;
            dcMotor = hardwareMap.get(DcMotorEx.class,name);
        }


        public builder targetMode(TargetMode targetMode) {
            this.targetMode = targetMode;
            return this;
        }

        public builder controller(Controller controller) {
            this.controller = controller;
            return this;
        }

        public builder ticksToDegrees(double ticksToDegrees) {
            this.ticksToDegrees = ticksToDegrees;
            return this;
        }

        public builder tolerance(double tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        public builder encoderReversed(boolean encoderReversed) {
            isEncoderReversed = encoderReversed;
            return this;
        }

        public builder direction(DcMotorSimple.Direction direction){
            dcMotor.setDirection(direction);
            return this;
        }

        public builder zeroPowerBehavior(DcMotor.ZeroPowerBehavior direction){
            dcMotor.setZeroPowerBehavior(direction);
            return this;
        }

        public builder setMaxPower(double max_power){
            this.max_power = max_power;
            return this;
        }

        public builder copy(SmartMotor motor){
            this.dcMotor.setDirection(motor.dcMotor.getDirection());
            this.dcMotor.setZeroPowerBehavior(motor.dcMotor.getZeroPowerBehavior());
            this.isEncoderReversed = motor.isEncoderReversed();
            this.controller = motor.getController();
            this.tolerance = motor.getTolerance();
            this.targetMode = motor.getTargetMode();
            this.ticksToDegrees = motor.getTicksToDegrees();
            this.max_power = motor.max_power;
            return this;
        }

        public SmartMotor build(){
            return new SmartMotor(dcMotor,name,ticksToDegrees,controller,isEncoderReversed,targetMode,tolerance,max_power);
        }
    }
}
