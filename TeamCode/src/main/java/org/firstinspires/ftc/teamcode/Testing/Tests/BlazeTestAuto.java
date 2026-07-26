package org.firstinspires.ftc.teamcode.Testing.Tests;

import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.BlazeAutoOpMode;

public class BlazeTestAuto extends BlazeAutoOpMode<AutoCore> {
    @Override
    public void setCore() {
        core = new AutoCore(Constants.Alliance.BLUE);
    }
    @Override
    public void addActions() {

    }

    @Override
    public void buildPaths() {

    }
}
