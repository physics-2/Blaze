package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.util.List;

public abstract class BlazeOpMode extends OpMode {
    List<LynxModule> allHubs;
    LynxModule.BulkCachingMode cacheType = LynxModule.BulkCachingMode.MANUAL;
    @Override
    public final void init() {
        Long startTime = System.currentTimeMillis();
        BlazeLogger.addDefaultLog("OPMODE","Initing...");
        setLynxCacheType(cacheType);
        BlazeCore.createConfig(hardwareMap);
        BlazeCore.setTelemetry(telemetry);
        BlazeCore.createModules();
        BlazeCore.addGamepads(gamepad1,gamepad2);

        initHotWrite();
        Long took = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("OPMODE","Init finished,took " + took + "ms");
    }

    public abstract void initHotWrite();
    public abstract void loopHotWrite();

    public abstract void onStartHotWrite();

    @Override
    public void start() {
        onStartHotWrite();
    }

    public void setLynxCacheType(LynxModule.BulkCachingMode cacheType) {
        this.cacheType = cacheType;
        allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(cacheType);
        }
    }

    @Override
    public void stop() {
        BlazeLogger.addDefaultLog("OPMODE", "Stopping and cleaning up...");
        BlazeCore.destroyHardware();
        ModuleRegistry.clear();
        onStopHotWrite();
    }

    public void onStopHotWrite(){}

    public void clearLynxCache(){
        for (LynxModule hub : allHubs) {
            hub.clearBulkCache();
        }
    }

    @Override
    public  void loop(){
        BlazeCore.addGamepads(gamepad1,gamepad2);
        if(this.cacheType == LynxModule.BulkCachingMode.MANUAL){
            clearLynxCache();
        }
        loopHotWrite();
    }

}
