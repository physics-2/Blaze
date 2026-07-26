package org.firstinspires.ftc.teamcode.blaze;

import com.bylazar.graph.GraphManager;
import com.bylazar.graph.PanelsGraph;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * An telemetry manager for multiple telemetries such as PP telemetry and standard telemetry
 * @author physics
 */
public class MultiDashTelemetry {
    Telemetry telemetry;
    TelemetryManager panelsTelemetry;
    GraphManager graph;


    public MultiDashTelemetry (Telemetry telemetry){
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        graph = PanelsGraph.INSTANCE.getManager();
        this.telemetry = telemetry;
        update();
    }

    public void addData(String caption,Object value){
        telemetry.addData(caption,value);
        panelsTelemetry.addData(caption,value);
    }



    public void addGraph(String caption,Number value){
        panelsTelemetry.addData(caption,value);
        graph.addData(caption,value);
        graph.update();

    }

    public void addLine(String message, Object value){
        telemetry.log().add(message + " : " + value);
        panelsTelemetry.addLine(message + " : " + value);
    }

    public void update(){
        telemetry.update();
        panelsTelemetry.update();
        graph.update();
    }
}
