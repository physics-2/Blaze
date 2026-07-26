package org.firstinspires.ftc.teamcode.Common.Configs;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.blaze.Anotations.HardwareConfig;
import org.firstinspires.ftc.teamcode.blaze.AutoWire.ElectronicsConfig;
import org.firstinspires.ftc.teamcode.blaze.Controllers.PIDFController;
import org.firstinspires.ftc.teamcode.blaze.BlazeCore;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.PowerTrain;


@HardwareConfig
public class NewTestConfig extends ElectronicsConfig {
    public NewTestConfig(){}
    PIDFController liftController = new PIDFController(new PIDFCoefficients(0,0,0,0));
    @Override
    protected void addElectronics(HardwareMap hardwareMap) {
        PowerTrain activeIntakePowerTrain = new PowerTrain.builder(hardwareMap,Constants.activeIntakePowerTrain)
                .addMotor(Constants.activeIntakeMotor, FORWARD)
                .addServo(Constants.shooterAngleServo, Servo.Direction.REVERSE)
                .startGroup("testGroup")
                .addMotor(Constants.activeIntakeMotor,FORWARD)
                .addMotor(Constants.activeIntakeMotor,FORWARD)
                .endGroup()
                .autowireTo("TestModule")
                .disable()
                .build();

        PowerTrain liftPowertrain = new PowerTrain.builder(hardwareMap,"lift")
                .addMotor(Constants.liftMotor, DcMotorSimple.Direction.FORWARD,0.5
                        ,liftController,false,1)
                .autowireTo("Lift")
                .build();


        BlazeCore.addPowerTrains(activeIntakePowerTrain,liftPowertrain);
    }
}
