package org.firstinspires.ftc.teamcode.Testing.Configs;

import com.acmerobotics.dashboard.config.Config;
import com.bylazar.configurables.annotations.Configurable;

@Config
@Configurable
public final class Constants {
    public enum Alliance {
        BLUE,
        RED
    }
    public static String activeIntakePowerTrain =  "activeIntakePowerTrain";

    public static String frontLeftDrive =  "leftFront";
    public static String frontRightDrive = "rightFront";
    public static String backLeftDrive = "leftRear";
    public static String backRightDrive = "rightRear";

    public static final String liftMotor = "liftMotor";
    public final static String activeIntakeMotor = "activeIntakeMotor";
    public static final String shooterAngleServo = "ShooterAngleServo";

    public static  String pushServo = "pushServo";
}
