package org.firstinspires.ftc.teamcode.Testing.Parts;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.blaze.BlazeCore;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.util.ArrayList;
import java.util.List;

public abstract class AutoBotCoreV5 extends OpMode {

    private final List<BlazingModule> blazingModules = new ArrayList<>();
    private SyncTopicBus bus;
    private long startTime = 0;

    // === НАШ autoSequence ===
    private List<Runnable> autoSequence;
    private int currentStep = 0;
    private boolean autoStarted = false;

    @Override
    public final void init() {
        BlazeCore.createConfig(hardwareMap);
        bus = BlazeCore.getFastBus();

        startTime = System.currentTimeMillis();


        setupModules();

        for (BlazingModule blazingModule : blazingModules) {
            blazingModule.onStart();
        }


        autoSequence = new ArrayList<>();


        initAuto();
    }

    @Override
    public final void loop() {

        for (BlazingModule blazingModule : blazingModules) {
            blazingModule.update();
        }


        if (!autoStarted) {
            autoStarted = true;
            runNextStep();
        }


        updateTelemetry();
    }

    @Override
    public void stop() {
        super.stop();
        BlazeCore.destroyHardware();
    }


    protected abstract void setupModules();


    protected abstract void initAuto();


    protected void registerModule(BlazingModule blazingModule) {
        blazingModules.add(blazingModule);
    }


    protected void addStep(Runnable action) {
        autoSequence.add(action);
    }


    protected void onNextStep(String eventTopic) {
        bus.subscribe(eventTopic, done -> runNextStep());
    }


    protected void runNextStep() {
        if (currentStep < autoSequence.size()) {
            autoSequence.get(currentStep).run();
            currentStep++;
        } else {

            onAutoComplete();
        }
    }


    protected void onAutoComplete() {

    }


    protected long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    protected void updateTelemetry() {
        telemetry.addData("Elapsed Time", getElapsedTime() + " ms");
        telemetry.addData("Auto Step", currentStep + " / " + autoSequence.size());

        for (BlazingModule blazingModule : blazingModules) {
            blazingModule.addTelemetry();
        }

        telemetry.update();
    }
}