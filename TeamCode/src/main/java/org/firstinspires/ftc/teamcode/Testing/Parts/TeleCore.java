package org.firstinspires.ftc.teamcode.Testing.Parts;

import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.Testing.Parts.BlazeModules.Lift;
import org.firstinspires.ftc.teamcode.blaze.Alliance;
import org.firstinspires.ftc.teamcode.blaze.Anotations.GetModule;
import org.firstinspires.ftc.teamcode.blaze.BlazeCommon;
import org.firstinspires.ftc.teamcode.blaze.Command;

public class TeleCore extends BlazeCommon {
    @GetModule
    Lift liftModule;

    @GetModule
    AutoDrive autoDrive;
    public TeleCore(Alliance alliance) {
        super(alliance);
    }

    @Override
    public void update() {
        super.update();
        if(Math.random() > 0.95){
            publishCommand(Lift.class,new Command("Up"));
        }

        if (Math.random() < 0.05){
            publishCommand(Lift.class,new Command("Down"));
        }
    }
}
