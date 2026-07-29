package org.firstinspires.ftc.teamcode.blaze.blaze.Actuators.Axon;


import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A continuous rotation servo
 *
 * @author Jackson(team 23511) & physics
 */
public class CRServoCustom{

    /**
     * The CR ServoEx motor object.
     */
    String id;
    protected CRServo crServo;
    public CRServoCustom(HardwareMap hardwareMap, String id) {
        this.id = id;
        crServo = hardwareMap.get(CRServo.class, id);
    }

    public String id() {
        return id;
    }

    public void setPower(double power){
        crServo.setPower(power);
    }

    public double getPower(){
        return crServo.getPower();
    }

}