package org.firstinspires.ftc.teamcode.Common.Parts.BlazeModules;

import com.bylazar.configurables.annotations.Configurable;

import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Anotations.AutowireActuator;
import org.firstinspires.ftc.teamcode.blaze.Anotations.Command;
import org.firstinspires.ftc.teamcode.blaze.Anotations.BlazeModule;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithTelemetry;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;


@BlazeModule
@Configurable
@InitWithTelemetry
public class Lift extends BlazingModule {
    public static double UP_POSE = 200;
    public static double DOWN_POSE = 0;


    @AutowireActuator
    SmartMotor liftMotor;
    public Lift(String name) {
        super(name);
    }

    public Lift(String name, MultiDashTelemetry telemetry) {
        super(name, telemetry);
    }

    @Override
    public void commonInit() {
        super.commonInit();

    }

    @Command()
    public void liftUp(){
        liftMotor.setTarget(UP_POSE);
    }

    @Command()
    public void liftDown(){
        liftMotor.setTarget(DOWN_POSE);
    }

    @Override
    public void addTelemetry() {
        super.addTelemetry();
        telemetry.addData("pose",liftMotor.getPosition());
    }
}
