package org.firstinspires.ftc.teamcode.blaze;


import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.util.ArrayList;
import java.util.List;

public abstract class BlazeAutoOpMode<Core extends BlazeCommon> extends BlazeOpMode {
    public Core  core;
    private long startTime;

    private List<Runnable> autoSequence;
    private int currentStep = 0;
    private boolean autoStarted = false;
    @Override
    public void initHotWrite() {
        autoSequence = new ArrayList<>();
        setCore();
        core.allModulesInit();
        core.init();
        core.setupAlliance();

        buildPaths();
        addActions();
    }

    public abstract void setCore();
    public abstract void addActions();

    protected void addStep(Runnable action) {
        autoSequence.add(action);
    }


    protected void onNextStep(String eventTopic) {
        SyncTopicBus.subscribe(eventTopic, done -> runNextStep());
    }

    protected void runNextStep() {
        if (currentStep < autoSequence.size()) {
            autoSequence.get(currentStep).run();
            currentStep++;
        } else {
            stop();
        }
    }

    @Override
    public void loop() {
        super.loop();
        if (!autoStarted) {
            autoStarted = true;
            runNextStep();
        }
    }


    @Override
    public void loopHotWrite() {
        core.update();
    }

    @Override
    public void onStartHotWrite() {
        core.onStart();
    }


    public abstract void buildPaths();
}
