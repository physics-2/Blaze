package org.firstinspires.ftc.teamcode.blaze.Actuators;


import org.firstinspires.ftc.teamcode.blaze.Controllers.Controller;

public interface Actuator {
    enum TargetMode{
        VELOCITY,
        POSITION,
        CUSTOM
    }
    Controller controller = null;

    void setTarget(double target);


    double getPosition();

    boolean isReady();
    default void setTolerance(double tolerance){}

    void reset();
    String getID();
    default void update(){}

    default void setMode(TargetMode targetMode){}

    default TargetMode getMode() {
        return TargetMode.POSITION;
    }


    default void setController(Controller controller){}

    default Controller getController(){return null;}

    default void setPower(double power){}
    default void scale(double from,double fromRaw
                        ,double to,double toRaw){}


}
