package org.firstinspires.ftc.teamcode.Common.Tests;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
import org.firstinspires.ftc.teamcode.Common.Parts.TeleCore;
import org.firstinspires.ftc.teamcode.blaze.BlazeTeleOp;

@TeleOp
public class CoreTest extends BlazeTeleOp<TeleCore> {
    @Override
    public void createCore() {
        core = new TeleCore(Constants.Alliance.BLUE);
    }
}
