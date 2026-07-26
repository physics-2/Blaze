package org.firstinspires.ftc.teamcode.Common.Parts.OnlyCode;

import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.Common.Configs.Constants;

public class PointCalculator {

    public PointCalculator(){

    }
    public double[] CalculateAzimuthAndDist(Pose botPose, Pose targetPose) {
        double dx = targetPose.getX() - botPose.getX(); // вперёд (+)
        double dy = targetPose.getY() - botPose.getY(); // вправо (+)

        double azimuth = Math.toDegrees(Math.atan2(dy, dx));


        double distance = botPose.distanceFrom(targetPose);
        return new double[]{azimuth, distance};
    }

}
