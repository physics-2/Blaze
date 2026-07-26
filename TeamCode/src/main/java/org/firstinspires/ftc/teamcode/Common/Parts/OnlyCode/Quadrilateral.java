package org.firstinspires.ftc.teamcode.Common.Parts.OnlyCode;

import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * Represents a simple (non-self-intersecting) quadrilateral defined by four vertices.
 * Uses Pose2D from FTC SDK to store vertex positions (heading is ignored).
 */
public class Quadrilateral {

    private final Pose[] vertices;

    /**
     * Constructs a quadrilateral from four Pose2D vertices.
     * The heading component of each Pose2D is ignored — only x and y are used.
     *
     * @param v1 First vertex
     * @param v2 Second vertex
     * @param v3 Third vertex
     * @param v4 Fourth vertex
     */
    public Quadrilateral(Pose v1, Pose v2, Pose v3, Pose v4) {
        this.vertices = new Pose[]{v1, v2, v3, v4};
    }

    /**
     * Constructs a quadrilateral from an array of exactly 4 Pose2D objects.
     * Headings are ignored.
     */
    public Quadrilateral(Pose[] vertices) {
        this.vertices = vertices; // FTC Pose2D is immutable, so safe to store directly
    }

    /**
     * Checks whether the given (x, y) point lies inside or on the edge of this quadrilateral.
     * Uses the ray-casting (even-odd) algorithm.
     *
     * @param x X coordinate (in the same units as vertices)
     * @param y Y coordinate (in the same units as vertices)
     * @return true if inside or on boundary
     */
    public boolean contains(double x, double y) {
        int intersections = 0;

        for (int i = 0; i < 4; i++) {
            double x1 = vertices[i].getX();
            double y1 = vertices[i].getY();
            double x2 = vertices[(i + 1) % 4].getX();
            double y2 = vertices[(i + 1) % 4].getY();

            // Skip horizontal edges
            if (y1 == y2) continue;

            // Ensure y1 < y2
            if (y1 > y2) {
                double tx = x1;
                double ty = y1;
                x1 = x2;
                y1 = y2;
                x2 = tx;
                y2 = ty;
            }

            // On horizontal edge?
            if (y == y1 && y == y2 && x >= Math.min(x1, x2) && x <= Math.max(x1, x2)) {
                return true;
            }

            // Ray casting: shoot ray to the right (+X)
            if (y > y1 && y <= y2) {
                double intersectX = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
                if (intersectX >= x) {
                    intersections++;
                }
            }
        }

        return (intersections % 2) == 1;
    }

    /**
     * Checks if a Pose2D (ignoring its heading) is inside the quadrilateral.
     * Assumes the same distance unit as used in the vertices.
     */
    public boolean contains(Pose pose) {
        if (pose == null) return false;
        // Извлекаем координаты в тех же единицах, что и вершины
        double x = pose.getX();
        double y = pose.getY();
        return contains(x, y);
    }

    // Optional: getter
    public Pose[] getVertices() {
        return vertices; // Pose2D immutable → safe
    }
}