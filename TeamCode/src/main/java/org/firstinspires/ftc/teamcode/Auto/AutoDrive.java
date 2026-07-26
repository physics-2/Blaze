package org.firstinspires.ftc.teamcode.Auto;


import com.pedropathing.follower.Follower;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.teamcode.blaze.Anotations.BlazeModule;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithTelemetry;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;


@BlazeModule(name = "drive")
@InitWithTelemetry
public class AutoDrive extends BlazingModule {

    private Follower follower;
    private boolean isFollowing = false;

    public AutoDrive(String name) {
        super(name);
        commonInit();
    }

    public AutoDrive(String name, org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry telemetry) {
        super(name, telemetry);
        commonInit();
    }

    @Override
    public void commonInit() {


        subscribe("drive/follow_path", pathObj -> {
            PathChain path = (PathChain) pathObj;
            follower.followPath(path, false);
            isFollowing = true;
        });
    }

    @Override
    public void update() {
        super.update();

        if (follower != null) {
            follower.update();

            if (isFollowing && !follower.isBusy()) {
                isFollowing = false;
                publish("drive/path_completed", true);
            }
        }
    }


    public void followPath(PathChain path) {
        publish("drive/follow_path", path);
    }
}