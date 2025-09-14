package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time; // The time objects were detected from json
    private ArrayList<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, ArrayList<DetectedObject> detectedList)
    {
        this.time = time;
        this.detectedObjects = detectedList;

    }

    public int getTime() {
        return time;
    }

    // Returns ArrayList of detected objects
    public ArrayList<DetectedObject> getList()
    {
        return detectedObjects;
    }
}
