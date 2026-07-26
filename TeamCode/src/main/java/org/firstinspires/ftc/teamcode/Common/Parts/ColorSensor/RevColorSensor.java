package org.firstinspires.ftc.teamcode.Common.Parts.ColorSensor;

import android.graphics.Color;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;
@Config
public class RevColorSensor implements ColorSensor{
    NormalizedColorSensor colorSensor;

    public  static float  gain = 1;
    public RevColorSensor(HardwareMap hardwareMap, String SensorName){
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, SensorName);
        if (colorSensor instanceof SwitchableLight) {
            ((SwitchableLight)colorSensor).enableLight(true);
        }
        colorSensor.setGain(gain);
    }

    public float[] getHSV(){
        float[] HSVValues = new  float[3];
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        Color.colorToHSV(colors.toColor(), HSVValues);
        return  HSVValues;
    }

    public NormalizedRGBA getRGB(){
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        return colors;
    }
}
