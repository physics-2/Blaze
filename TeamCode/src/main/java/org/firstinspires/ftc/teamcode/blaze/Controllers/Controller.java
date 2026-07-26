package org.firstinspires.ftc.teamcode.blaze.Controllers;

public interface Controller {
    /**
     * The function that does some math to calculate output
     * @param target wanted name
     * @param feedback current name
     */
    double calculate(double target,double feedback);
    double calculate(double error);

    void setCoefficients(Object coef);

    double getI();

    void setI(double i);

    void setIMax(double v);
    double getControl();

    void reset();
}
