package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id; // The id of the object
    private int time; // The time the object was tracekd
    private ArrayList<CloudPoint> cloudPoints; // List of lists of cloud points

    public StampedCloudPoints() {}

    public String getId()
    {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public ArrayList<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }

    public void setCloudPoints(ArrayList<CloudPoint> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }


    @Override
    public String toString() {
        return "StampedCloudPoints{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", cloudPoints=" + cloudPoints +
                '}';
    }
}
