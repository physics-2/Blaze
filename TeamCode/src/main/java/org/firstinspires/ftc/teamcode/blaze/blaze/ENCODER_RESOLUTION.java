package org.firstinspires.ftc.teamcode.blaze.blaze;

public enum ENCODER_RESOLUTION {
    YELLOWJACKET1150RPM(2.4725274725274726),
    YELLOWJACKET312RPM(0.6695183187651106),
    YELLOWJACKET435RPM(0.9362808842652796),
    YELLOWJACKET6000RPM(12),
    REVHEXBARE(12),
    REVHEX40TO1(0.32142857142857145),
    REVHEX20TO1(0.6428571428571429);
    double ticksToDegrees;


    ENCODER_RESOLUTION(double ticksToDegrees){
        this.ticksToDegrees = ticksToDegrees;
    }
    public double get(){
        return ticksToDegrees;
    }
}
