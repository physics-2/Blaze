package org.firstinspires.ftc.teamcode.Common.Parts.Camera;

import java.util.List;

public interface AprilTagCam {
    public double getAprilTagDistance(int id);
    public double getAprilTagHightOffset(int id);
    public double getAprilTagSideOffset(int id);
    public List<Integer> getAllApriltagsID();
    public boolean isAprilTagSeen(int id);
    public void update();
}
