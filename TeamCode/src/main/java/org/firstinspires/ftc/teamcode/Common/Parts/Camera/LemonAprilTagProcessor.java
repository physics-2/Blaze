package org.firstinspires.ftc.teamcode.Common.Parts.Camera;

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.Common.Parts.OnlyCode.ShooterCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LemonAprilTagProcessor implements AprilTagCam {
    final Limelight3A limelight;
    LLResult result;
    List<LLResultTypes.FiducialResult> fiducials;

    public  LemonAprilTagProcessor(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(0);
        limelight.start();
    }

    public double getAprilTagDistance(int id) {
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if(fiducial.getFiducialId() == id) {
                Position position = fiducial.getRobotPoseTargetSpace().getPosition();
                return  position.z;
            }
        }
        return 0;
    }


    public double getAprilTagHightOffset(int id) {
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if(fiducial.getFiducialId() == id) {
                Position position = fiducial.getRobotPoseTargetSpace().getPosition();
                return  position.y;
            }
        }
        return 0;
    }


    public double getAprilTagSideOffset(int id) {
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if(fiducial.getFiducialId() == id) {
                Position position = fiducial.getRobotPoseTargetSpace().getPosition();
                return  position.x;
            }
        }
        return 0;
    }


    public List<Integer> getAllApriltagsID() {
        List<Integer> allApriltags  = new ArrayList<>();
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            allApriltags.add(fiducial.getFiducialId());
        }
        return allApriltags;
    }


    public boolean isAprilTagSeen(int id) {
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if(fiducial.getFiducialId() == id) {
                return  true;
            }
        }
        return false;
    }

    public void update() {
        LLResult result = limelight.getLatestResult();
        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
    }
}
