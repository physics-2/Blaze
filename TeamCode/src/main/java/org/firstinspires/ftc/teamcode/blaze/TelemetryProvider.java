package org.firstinspires.ftc.teamcode.blaze;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public interface TelemetryProvider {
    MultiDashTelemetry create(Telemetry coreTelemetry);
}