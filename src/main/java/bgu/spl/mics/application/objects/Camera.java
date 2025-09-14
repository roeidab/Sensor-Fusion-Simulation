package bgu.spl.mics.application.objects;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    //members
    private int id; // Camera id
    private final int frequency; // Time interval at which the camera sends new events (time + frequency)
    private STATUS status; // enum - up/down/error
    // List of stamped DetectedObject - Time-stamped objects the camera detected, information from json file
    private final ArrayList<StampedDetectedObjects> detectedObjectsList; // Sorted by time!!!!
    private int nextDetection;
    private List<StampedDetectedObjects> lastDetectedObjects;
    private String errorDescription;

    //methods
    public Camera(int id, int frequency, ArrayList<StampedDetectedObjects> json_CameraInfo){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.DOWN;
        this.detectedObjectsList = json_CameraInfo;
        this.nextDetection = 0;
        errorDescription = "";
    }

    public String getErrorDescription()
    {
        return errorDescription;
    }


    /**
     * Prepares data for the current tick by processing detected objects or handling status changes.
     * @return stampedDetectedObjects if valid for currentTick from database
     * @param currentTick
     *            The current simulation tick (must be non-negative).
     * @pre: currentTick >= 0
     * @pre: this.status is STATUS.UP
     * @pre: this.nextStampedDetectedObjects() may return null or a valid StampedDetectedObjects instance.
     *
     * @post: If the next stamped object is valid and the objects time is >= currentTick, returns the stampedObjects
     * @post: If valid time object with "ERROR" id, this.status == STATUS.ERROR
     * @post: If there's no new stamped object with smaller currentTick received, returns null
     * @post: If no stamped objects remain, this.status == STATUS.DOWN
     *
     */

    public StampedDetectedObjects prepareData(int currentTick)
    {
        StampedDetectedObjects stamped_objects = this.nextStampedDetectedObjects();
        if ( this.status == STATUS.UP && stamped_objects != null )
        {
            ArrayList<DetectedObject> objsList = stamped_objects.getList();

            if ( this.status == STATUS.UP &&
                    currentTick >= stamped_objects.getTime() + this.frequency)
            {
                for (DetectedObject detectedObject: objsList)
                {
                    if (detectedObject.getId().equals("ERROR"))
                    {
                        this.status = STATUS.ERROR;
                        this.errorDescription = detectedObject.getDescription();
                        return stamped_objects;
                    }
                }
                this.next(); // Didn't catch error, can proceed to next objects in db
            }
            else { return null; } // Do nothing;
        }
        else // Reached the end of the stamped detected objects list
        {
            this.status = STATUS.DOWN;
        }
        return stamped_objects;
    }

    public StampedDetectedObjects LastDetectedObjects()
    {
        return detectedObjectsList.get(nextDetection - 1);
    }

    private void next()
    {
        this.nextDetection += 1;

    }

    public void setStatus(STATUS stat)
    {
        this.status = stat;
    }

    public STATUS getStatus()
    {
        return this.status;
    }

    public int getFrequency(){ return frequency; }

    public int getID() { return id; }

    private StampedDetectedObjects nextStampedDetectedObjects()
    {
        if (detectedObjectsList != null && nextDetection < detectedObjectsList.size() )
        {
            return detectedObjectsList.get(nextDetection);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Camera: {" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                '}';
    }
}
