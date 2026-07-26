package org.firstinspires.ftc.teamcode.Common.Tests;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.BlazeTeleOp;


@TeleOp
public class BlazeTests extends BlazeTeleOp<TestCore> {
    @Override
    public void createCore() {
        core = new TestCore(Constants.Alliance.RED);
    }
}
