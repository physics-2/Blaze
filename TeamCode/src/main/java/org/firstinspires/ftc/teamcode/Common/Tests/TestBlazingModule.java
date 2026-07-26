package org.firstinspires.ftc.teamcode.Common.Tests;

import static org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus.getLatest;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.Anotations.AutowireActuator;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartServo;
import org.firstinspires.ftc.teamcode.blaze.Anotations.BlazeModule;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithTelemetry;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.ActuatorGroup;


@InitWithTelemetry
@BlazeModule
public class TestBlazingModule extends BlazingModule {

    @AutowireActuator(name = Constants.activeIntakeMotor)
    SmartMotor motor;

    @AutowireActuator(name = Constants.shooterAngleServo)
    SmartServo servo;

    @AutowireActuator
    ActuatorGroup testGroup;

    public TestBlazingModule(String name) {
        super(name);
    }

    public TestBlazingModule(String name, MultiDashTelemetry telemetry) {
        super(name, telemetry);

    }



    String message;
    boolean needsRun = false;
    @Override
    public void commonInit() {
        super.commonInit();
        publish("debug","str");
        subscribe("debug",command -> {

                String strCommand = ((String) command);
                manageCommand(strCommand);

        });

    }

    private void manageCommand(String command){
        if(command.startsWith("addDebug:")){
            message = command.replace("addDebug:","");
            needsRun = true;

        }
    }

    @Override
    public void addTelemetry() {
        super.addTelemetry();
        telemetry.addData("Motor alive " , motor.toString());
        telemetry.addData("Servo",servo.toString());
        telemetry.addData("Group", testGroup.getActuators().toString());
        telemetry.addData("Got : ",getLatest("test"));
        if(needsRun){
            telemetry.addData("Add debug",message);
            needsRun = false;
        }

    }

    long time = 1;
    @Override
    public void update() {
        time = System.currentTimeMillis();
        super.update();
        motor.setPower(1);
        servo.setTarget(1);
        testGroup.setTarget(2);
        publish("test" , System.currentTimeMillis() - time);
    }
}
