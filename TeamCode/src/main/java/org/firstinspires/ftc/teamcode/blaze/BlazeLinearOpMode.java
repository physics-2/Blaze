package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.List;

public abstract class BlazeLinearOpMode extends LinearOpMode {
    List<LynxModule> allHubs;
    LynxModule.BulkCachingMode defCacheType = LynxModule.BulkCachingMode.AUTO;
    @Override
    public void runOpMode() throws InterruptedException {
        setLynxCacheType(defCacheType);
        BlazeCore.createConfig(hardwareMap);
        BlazeCore.setTelemetry(telemetry);
        BlazeCore.createModules();
        runHotWriteOpMode();
    }

    public void setLynxCacheType(LynxModule.BulkCachingMode cacheType) {
        allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(cacheType);
        }
    }


    public void clearLynxCache(){
        for (LynxModule hub : allHubs) {
            hub.clearBulkCache();
        }
    }
    public abstract void runHotWriteOpMode();

}
