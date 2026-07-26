package org.firstinspires.ftc.teamcode.Common.Parts.OnlyCode;

import com.acmerobotics.dashboard.config.Config;
import com.bylazar.configurables.annotations.Configurable;

@Config
@Configurable
public class ShooterCalculator {
    public static double wheelDiameter = 0.03;//0.1016 actual
    public static double slipFactor = 0.15;
    public static double G = 9.81;

    public static double thetaMin = 67;
    public static  double thetaMax = 677;
    public static double dTheta =0.01;
    public static double maxMotorRPM = 7000;
    public static double GoalHeight = 1.4;

    public ShooterCalculator(){

    }

    public double convertInchToMeter(double inch){
        return inch * 0.0254;
    }

    public static double speedToRPM(double speed_m_s) {
        return( ( speed_m_s) / (Math.PI * wheelDiameter)) * 60;
    }

    public static double rpmToSpeed(double rpm) {
        return (rpm * Math.PI * wheelDiameter) / 60.0;
    }

    public double[] calculateShooterParams(double distanceToWall){
        for (double theta = Math.toRadians(thetaMax); theta >=Math.toRadians(thetaMin); theta -= Math.toRadians(dTheta)) {
            double numerator =G * Math.pow(distanceToWall,2);
            double denominator = 2 * Math.pow(Math.cos(theta),2) * (distanceToWall *Math.tan(theta) - GoalHeight);
            if (denominator <= 0) continue;
            if (numerator <= 0) continue;

            double speed = Math.sqrt(numerator / denominator) ;

            double motorRPM =speedToRPM (speed)  * (1 + slipFactor);

            if (motorRPM <= maxMotorRPM) {
                return new double[]{motorRPM , Math.toDegrees(theta)};
            }

        }
        return new double[]{maxMotorRPM, thetaMin};
    }


}
