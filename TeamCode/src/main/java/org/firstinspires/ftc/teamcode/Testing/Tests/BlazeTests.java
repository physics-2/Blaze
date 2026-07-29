package org.firstinspires.ftc.teamcode.Testing.Tests;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.Alliance;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazeTeleOp;


@TeleOp
public class BlazeTests extends BlazeTeleOp<TestCore> {
    @Override
    public void createCore() {
        core = new TestCore(Alliance.RED);
    }
}
