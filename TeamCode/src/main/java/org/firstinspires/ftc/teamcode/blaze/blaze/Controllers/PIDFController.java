package org.firstinspires.ftc.teamcode.blaze.blaze.Controllers;

import static java.lang.Math.abs;

import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class PIDFController implements Controller {
    private PIDFCoefficients coefficients;
    private double I = 0;
    private double errorOld = 0;
    private double control = 0;
    private double IMax = 1;
    private final Timer dtTimer = new Timer();

    public PIDFController(PIDFCoefficients coefficients){
        this.coefficients = coefficients;
    }
    public PIDFController(PIDFCoefficients coefficients,double IMax){
        this.IMax = IMax;
        this.coefficients = coefficients;
    }

    public double calculate(double target,double feedback){
        double dt = dtTimer.getElapsedTimeSeconds();

        double error = target - feedback;

        double P = error * coefficients.p;
        I += error * coefficients.i;

        I += error * coefficients.i * dt;
        if (abs(I) > IMax) {
            I = IMax * Math.signum(I);
        }


        double D = (error - errorOld) / dt * coefficients.d;
        errorOld = error;

        double windUp = coefficients.f * target;
        control = P + I + D + windUp;
        errorOld = error;
        if (abs(control) > 1) control = 1 * Math.signum(control);
        dtTimer.resetTimer();
        return control;
    }


    @Deprecated
    /** DO NOT USE THIS,it can't work without feedback
     *
     */
    @Override
    public double calculate(double error) {
        return 0;
    }

    @Override
    public void setCoefficients(Object coef) {
        this.coefficients = ((PIDFCoefficients) coef);
    }

    public void setCoefficients(PIDFCoefficients coefficients) {
        this.coefficients = coefficients;
    }

    public double getI() {
        return I;
    }
    public void setIMax(double IMax) {
        this.IMax = IMax;
    }

    public void setI(double i) {
        I = i;
    }

    public double getControl() {
        return control;
    }

    @Override
    public String toString() {
        return "PIDFController{" +
                "control=" + control +
                ", coefficients=" + coefficients +
                '}';
    }

    public void reset(){
        I = 0;
        control = 0;
    }
}
