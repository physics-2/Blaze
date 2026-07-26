package org.firstinspires.ftc.teamcode.Common.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;

@Config
@Configurable
@Autonomous(name = "Test: Motor test",group = "Tests")
public class MotorTest extends LinearOpMode {
    DcMotorEx testMotor;
    public static double motorSpeed = 0;
    public static double pose = 0.785;//0.969
    public static String motorName = Constants.activeIntakeMotor;


    @Override
    public void runOpMode() throws InterruptedException {
        testMotor = hardwareMap.get(DcMotorEx.class,motorName);
        Timer timer = new Timer();
        timer.resetTimer();
        MultiDashTelemetry Telemetry = new MultiDashTelemetry(telemetry);
        waitForStart();
        while (opModeIsActive()){
            testMotor.setPower(motorSpeed);

            Telemetry.addData("Current",testMotor.getCurrent(CurrentUnit.AMPS));
            Telemetry.addData("RPM",(testMotor.getVelocity(AngleUnit.DEGREES)  * 30));
            Telemetry.addData("Encoder",testMotor.getCurrentPosition());
            Telemetry.update();
        }
    }
}
