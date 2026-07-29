package org.firstinspires.ftc.teamcode.blaze.Actuators.Axon;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * An extended wrapper class for AnalogInput absolute encoders.
 *
 * @author Saket(team 23511) & physics
 */
public class AnalogEncoder implements Encoder{
    private final AnalogInput encoder;
    private final String id;
    private double offset = 0.0;
    private final double range;
    private final AngleUnit angleUnit;
    private final boolean reversed;
    private final ElapsedTime timer = new ElapsedTime();
    private double lastPos;
    private double vel = 0;

    /**
     * The constructor for absolute analog encoders
     * @param hardwareMap the hardwareMap
     * @param id the ID of the encoder as configured
     */
    public AnalogEncoder(HardwareMap hardwareMap, String id) {
        this.encoder = hardwareMap.get(AnalogInput.class, id);
        this.angleUnit = AngleUnit.DEGREES;
        this.range = 3.3;
        this.id = id;
        reversed = false;
    }
    public AnalogEncoder(HardwareMap hardwareMap, String id, double range, AngleUnit angleUnit) {
        this.encoder = hardwareMap.get(AnalogInput.class, id);
        this.angleUnit = angleUnit;
        this.range = range;
        this.id = id;
        reversed = false;
    }


    /**
     * Sets an angular offset for any future values returned when reading the encoder
     * @param offset The angular offset in the units specified by the user previously
     * @return The object itself for chaining purposes
     */
    public AnalogEncoder zero(double offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public double getPosition() {
        double newPos = normalizeAngle(
                (!reversed ? 1 - getVoltage() / range : getVoltage() / range) * returnMaxForAngleUnit(angleUnit) - offset,
                true,
                angleUnit
        );
        vel = (newPos - lastPos) / timer.seconds();
        timer.reset();
        lastPos = newPos;
        return lastPos;
    }

    public double getVelocity() {
        return vel;
    }
    public double getVoltage(){
        return encoder.getVoltage();
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


}