
package org.firstinspires.ftc.teamcode.Common.pedroPathing;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.Encoder;
import com.pedropathing.ftc.localization.constants.TwoWheelConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

//
@Configurable
public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(13.4)
            .forwardZeroPowerAcceleration(-35.924)
            .lateralZeroPowerAcceleration(-70.4682)
             .translationalPIDFCoefficients(new PIDFCoefficients(0.16, 0.0001, 0.014 ,0.04))
            .useSecondaryHeadingPIDF(false)
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.008,0.000001,0.001,0.6,0))
            .useSecondaryDrivePIDF(false)

             .headingPIDFCoefficients(new PIDFCoefficients(0.75,0,0.14,0.05))
            .useSecondaryHeadingPIDF(false)
             .centripetalScaling(0.0009);


    public static  TwoWheelConstants  twoWheelConstants= new TwoWheelConstants()
            .forwardEncoder_HardwareMapName(org.firstinspires.ftc.teamcode.Common.Configs.Constants.backRightDrive)
            .strafeEncoder_HardwareMapName(org.firstinspires.ftc.teamcode.Common.Configs.Constants.activeIntakeMotor)
            .forwardEncoderDirection(Encoder.FORWARD)
            .strafeEncoderDirection(Encoder.FORWARD)
            .strafePodX(2.59605)//-3.305 -1.342 -3.65 -4.29 -0.944 -2.0453
             .forwardPodY(2.573)//2.659 3.088 3.550 1.982 2.403 1.756

            .forwardTicksToInches(0.000559670437333333)
            .strafeTicksToInches(0.0005827043454966667)
            .IMU_HardwareMapName("imu")
            .IMU_Orientation(
                    new RevHubOrientationOnRobot(
                    RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                    RevHubOrientationOnRobot.UsbFacingDirection.UP
                    )
            );


    public static MecanumConstants mecanumConstants = new MecanumConstants()
            .rightFrontMotorName(org.firstinspires.ftc.teamcode.Common.Configs.Constants.frontRightDrive)
            .rightRearMotorName((org.firstinspires.ftc.teamcode.Common.Configs.Constants.backRightDrive))
            .leftRearMotorName((org.firstinspires.ftc.teamcode.Common.Configs.Constants.backLeftDrive))
            .leftFrontMotorName(org.firstinspires.ftc.teamcode.Common.Configs.Constants.frontLeftDrive)
            .leftFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(77)
            .yVelocity(39.2805)
            .useBrakeModeInTeleOp(true)
            .useVoltageCompensation(true);

    public static PathConstraints pathConstraints = new PathConstraints(0.91,
            50,
            0.6,
            4);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(mecanumConstants)
                .twoWheelLocalizer(twoWheelConstants)
                .build();
    }
}

