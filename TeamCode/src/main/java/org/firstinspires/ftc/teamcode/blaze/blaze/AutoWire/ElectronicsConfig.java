package org.firstinspires.ftc.teamcode.blaze.blaze.AutoWire;

import com.qualcomm.robotcore.hardware.HardwareMap;

public abstract class ElectronicsConfig {
    public void addElectronicsFull(HardwareMap hardwareMap){
        addElectronics(hardwareMap);
    }
    protected abstract void addElectronics(HardwareMap hardwareMap);


}
