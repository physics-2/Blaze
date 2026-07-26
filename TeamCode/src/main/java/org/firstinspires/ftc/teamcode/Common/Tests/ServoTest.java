package org.firstinspires.ftc.teamcode.Common.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.BlazeLinearOpMode;

@Config
@Configurable
@Autonomous(name = "Test: Servo test",group = "Tests")
public class ServoTest extends BlazeLinearOpMode {

    public static double position = 0.415;
    public static String name = Constants.pushServo;
    @Override
    public void runHotWriteOpMode() {
        Servo servo = hardwareMap.get(Servo.class,name);
        servo.setDirection(Servo.Direction.REVERSE);
        waitForStart();
        while (opModeIsActive()){
           servo.setPosition(position);
        }
    }
}
