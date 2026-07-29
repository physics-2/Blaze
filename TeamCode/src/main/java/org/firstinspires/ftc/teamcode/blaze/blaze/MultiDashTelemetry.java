package org.firstinspires.ftc.teamcode.blaze.blaze;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class MultiDashTelemetry {
    protected final Telemetry coreTelemetry;

    public MultiDashTelemetry(Telemetry coreTelemetry) {
        this.coreTelemetry = coreTelemetry;
    }

    public void addData(String caption, Object value) {
        coreTelemetry.addData(caption, value);
    }

    public void addGraph(String caption, Number value) {
    }

    public void addLine(String message, Object value) {
        coreTelemetry.log().add(message + " : " + value);
    }

    public void update() {
        coreTelemetry.update();
    }
}