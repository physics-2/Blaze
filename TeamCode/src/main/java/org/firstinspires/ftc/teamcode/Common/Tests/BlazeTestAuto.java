package org.firstinspires.ftc.teamcode.Common.Tests;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;
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
