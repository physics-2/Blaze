package org.firstinspires.ftc.teamcode.Common.Tests;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.BlazeCommon;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

public class TestCore extends BlazeCommon {
    TestBlazingModule module;
    public TestCore(Constants.Alliance alliance) {
        super(alliance);
        module = module(TestBlazingModule.class);
    }

    @Override
    public void update() {
        super.update();
        if(Math.random() > 0.95){
            SyncTopicBus.publish("debug","addDebug:" + "debug");
        }

    }

}
