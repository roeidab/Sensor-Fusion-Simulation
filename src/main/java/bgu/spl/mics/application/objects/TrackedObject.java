package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private int time; // The time the object was tracked - from lidar
    private String id; // ID of the object - from lidar
    private String description; // Description of the object - from camera
    private ArrayList<CloudPoint> coordinates; // object cloudpoints from lidar

    public TrackedObject(int time, String id, String description, ArrayList<CloudPoint> cords)
    {
        this.time = time;
        this.id = id;
        this.description = description;
        this.coordinates = cords;
    }
    public int getTime(){
        return time;
    }
    public String getID(){
        return id;
    }
    public String getDesc(){
        return description;
    }
    public ArrayList<CloudPoint> getCloudPoints(){
        return coordinates;
    }
}
