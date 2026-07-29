package org.firstinspires.ftc.teamcode.blaze.blaze.Controllers;

import static java.lang.Math.abs;

import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

public class PIDController implements Controller {
    private PIDCoefficients coefficients;
    private double I = 0;
    private double IMax = 0.2;
    private double errorOld = 0;
    private double control = 0;
    Timer dtTimer = new Timer();

    public PIDController(PIDCoefficients coefficients) {
        this.coefficients = coefficients;
    }

    public PIDController(PIDCoefficients coefficients, double IMax) {
        this.IMax = IMax;
        this.coefficients = coefficients;
    }

    @Override
    public double calculate(double target, double feedback) {
        double dt = dtTimer.getElapsedTimeSeconds();
        double error = target - feedback;

        double P = error * coefficients.p;


        I += error * coefficients.i * dt;
        if (abs(I) > IMax) {
            I = IMax * Math.signum(I);
        }

        double D = (error - errorOld) / dt * coefficients.d;
        errorOld = error;

        control = P + I + D;

        if (abs(control) > 1) {
            control = Math.signum(control);
        }

        dtTimer.resetTimer();
        return control;
    }

    @Override
    public double calculate(double error) {
        double dt = dtTimer.getElapsedTimeSeconds();

        double P = error * coefficients.p;

        // Integrate with respect to time
        I += error * coefficients.i * dt;
        if (abs(I) > IMax) {
            I = IMax * Math.signum(I);
        }

        // Derivative term uses change in error over time
        double D = (error - errorOld) / dt * coefficients.d;
        errorOld = error;

        control = P + I + D;

        // Output clamping to [-1, 1]
        if (abs(control) > 1) {
            control = Math.signum(control);
        }

        dtTimer.resetTimer();
        return control;
    }

    @Override
    public void setCoefficients(Object coef) {
        this.coefficients = ((PIDCoefficients) coef);
    }

    public void setCoefficients(PIDCoefficients coefficients) {
        this.coefficients = coefficients;
    }

    public void setIMax(double IMax) {
        this.IMax = IMax;
    }

    @Override
    public double getControl() {
        return control;
    }


    @Override
    public String toString() {
        return "PIDController{" +
                "control=" + control +
                ", coefficients=" + coefficients +
                '}';
    }

    public double getI() {
        return I;
    }

    public void setI(double i) {
        I = i;
    }

    public void reset() {
        I = 0;
        control = 0;
        errorOld = 0;
    }
}