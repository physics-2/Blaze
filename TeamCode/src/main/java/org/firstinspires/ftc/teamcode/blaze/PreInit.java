package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(group = "AAA Blaze",name = "Pre Init Blaze")
public class PreInit extends BlazeOpMode {
    @Override
    public void initHotWrite() {
        telemetry.addData("Init finished!","");
        BlazeLogger.addDefaultLog("OPMODE/SYS","Pre init run,cache created");
        telemetry.update();
    }

    @Override
    public void loopHotWrite() {
        stop();
    }

    @Override
    public void onStartHotWrite() {
        stop();
    }
}
