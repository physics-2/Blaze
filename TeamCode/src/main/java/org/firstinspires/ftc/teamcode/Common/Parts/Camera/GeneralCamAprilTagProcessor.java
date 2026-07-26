/* Copyright (c) 2023 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Common.Parts.Camera;

import static android.os.SystemClock.sleep;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Config
public class GeneralCamAprilTagProcessor implements AprilTagCam{
    LinearOpMode opMode;
    private org.firstinspires.ftc.vision.apriltag.AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private final HardwareMap hardwareMap;
    List<AprilTagDetection> currentDetections = new ArrayList<>();
    private double yOffset;
    private double xOffset;
    private int tagID;
    private double dist;
    MultiDashTelemetry telemetry;

    public GeneralCamAprilTagProcessor(HardwareMap hardwareMap, LinearOpMode opMode) {

        this.hardwareMap = hardwareMap;
        initAprilTag();
        FtcDashboard.getInstance().startCameraStream(visionPortal, 60);
    }

    public GeneralCamAprilTagProcessor(HardwareMap hardwareMap) {

        this.hardwareMap = hardwareMap;
        initAprilTag();
        FtcDashboard.getInstance().startCameraStream(visionPortal, 60);
    }

    public GeneralCamAprilTagProcessor(HardwareMap hardwareMap, LinearOpMode opMode, MultiDashTelemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
        initAprilTag();
        FtcDashboard.getInstance().startCameraStream(visionPortal, 60);
    }

    public GeneralCamAprilTagProcessor(HardwareMap hardwareMap, MultiDashTelemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
        initAprilTag();
        FtcDashboard.getInstance().startCameraStream(visionPortal, 60);
    }

    private void initAprilTag() {
        aprilTag = new org.firstinspires.ftc.vision.apriltag.AprilTagProcessor.Builder()
                //.setDrawAxes(true)
                .setDrawCubeProjection(true)
                //.setDrawTagOutline(true)
                .setTagFamily(org.firstinspires.ftc.vision.apriltag.AprilTagProcessor.TagFamily.TAG_36h11)
                .setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary())
                .setOutputUnits(DistanceUnit.METER, AngleUnit.DEGREES)
                .build();
        // Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
        // Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
        // Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second (default)
        // Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second (default)
        aprilTag.setDecimation(3);
        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        builder.setCameraResolution(new Size(800, 600));
        builder.setStreamFormat(VisionPortal.StreamFormat.MJPEG);
        builder.addProcessor(aprilTag);
        visionPortal = builder.build();
    }

    public double getAprilTagDistance(int id) {
        double distance = 0;
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata.id == id) {
                distance = detection.ftcPose.range;
            }
        }
        return distance;
    }

    public AprilTagPoseFtc  getPose(int id) {
        AprilTagPoseFtc poseFtc = null;
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata.id == id) {
                poseFtc = detection.ftcPose;
            }
        }
        return poseFtc;
    }

    public double getAprilTagHightOffset(int id) {
        double offset = 0;
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata.id == id) {
                offset = detection.ftcPose.y;
            }
        }
        return offset;
    }

    public double getAprilTagSideOffset(int id) {
        double offset = 0;
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata.id == id) {
                offset = detection.ftcPose.bearing;
            }
        }
        return offset;
    }

    public List<Integer> getAllApriltagsID() {
        List<Integer> aprilTagsID = new ArrayList<>();
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                aprilTagsID.add(detection.id);
            }
        }
        return aprilTagsID;
    }

    public boolean isAprilTagSeen(int id) {
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata.id == id) {
                return true;
            }
        }
        return false;
    }

    public void update(){
        currentDetections = aprilTag.getDetections();
    }

    public  void  setManualExposure(int exposureMS, int gain) {
        // Wait for the camera to be open, then use the controls

        if (visionPortal == null) {
            return;
        }

        // Make sure camera is streaming before we try to set the exposure controls
        if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {

            while ((visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING)) {
                sleep(20);
            }
        }

        // Set camera controls unless we are stopping.

            ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                exposureControl.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }
            exposureControl.setExposure(exposureMS, TimeUnit.MILLISECONDS);
            sleep(20);
            GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
            gainControl.setGain(gain);
            sleep(20);

    }

    public void addTelemetry(){
        if (telemetry == null) return;
        telemetry.addData("CAM","");
        telemetry.addData("AprilTagList ",getAllApriltagsID());
        for(AprilTagDetection detection : currentDetections){
            telemetry.addData("Distance to " + detection.metadata.id,getAprilTagDistance( detection.metadata.id));
            telemetry.addData("Offset to " + detection.metadata.id,getAprilTagSideOffset( detection.metadata.id));
        }
    }

}
