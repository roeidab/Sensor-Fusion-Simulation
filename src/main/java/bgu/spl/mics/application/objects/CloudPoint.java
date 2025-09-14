package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */

//Ignore Z axis in the json
public class CloudPoint {
    private double x;
    private double y;

    public CloudPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    // Default constructor
    public CloudPoint() {}

    // Getters and setters
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


    @Override
    public String toString() {
        return "CloudPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
