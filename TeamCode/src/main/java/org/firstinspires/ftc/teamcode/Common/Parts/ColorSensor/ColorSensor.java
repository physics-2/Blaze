package org.firstinspires.ftc.teamcode.Common.Parts.ColorSensor;

import com.qualcomm.robotcore.hardware.NormalizedRGBA;

public interface ColorSensor {
    public NormalizedRGBA getRGB();
    enum BallColor{
        PURPLE,
        GREEN,
        NONE
    }

}
