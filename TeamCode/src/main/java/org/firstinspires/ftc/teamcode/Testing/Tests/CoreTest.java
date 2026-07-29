package org.firstinspires.ftc.teamcode.Testing.Tests;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.Testing.Parts.TeleCore;

import org.firstinspires.ftc.teamcode.blaze.Alliance;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazeTeleOp;

@TeleOp
public class CoreTest extends org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazeTeleOp<TeleCore> {
    @Override
    public void createCore() {
        core = new TeleCore(Alliance.BLUE);
    }
}
