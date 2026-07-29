package org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem;

import org.firstinspires.ftc.teamcode.blaze.BlazeCommon;
import org.firstinspires.ftc.teamcode.blaze.BlazeOpMode;

public abstract class BlazeTeleOp<Core extends BlazeCommon> extends BlazeOpMode {
    protected Core core;
    @Override
    public void initHotWrite() {
        createCore();
        core.allModulesInit();
        core.init();
        core.setupAlliance();
    }

    public abstract void createCore();

    @Override
    public void onStopHotWrite() {
        core.onStop();
    }

    @Override
    public void loopHotWrite() {
        core.update();
    }

    @Override
    public void onStartHotWrite() {
        core.onStart();
    }
}
