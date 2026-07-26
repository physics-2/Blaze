package org.firstinspires.ftc.teamcode.Testing.Parts;

import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.Testing.Parts.BlazeModules.Lift;
import org.firstinspires.ftc.teamcode.blaze.Anotations.GetModule;
import org.firstinspires.ftc.teamcode.blaze.BlazeCommon;

public class TeleCore extends BlazeCommon {
    @GetModule
    Lift liftModule;

    @GetModule
    AutoDrive autoDrive;
    public TeleCore(Constants.Alliance alliance) {
        super(alliance);
    }

    @Override
    public void update() {
        super.update();
        if(Math.random() > 0.95){
            publishCommand(Lift.class,"Up");
        }

        if (Math.random() < 0.05){
            publishCommand(Lift.class,"Down");
        }
    }
}
