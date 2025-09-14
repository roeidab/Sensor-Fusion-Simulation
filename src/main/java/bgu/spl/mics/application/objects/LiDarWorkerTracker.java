package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id; // The ID of the LiDar
    private int frequency; // The time interval at which the LiDar sends new event ( time + freq )
    private STATUS status; // up/down/error
    private List<TrackedObject> lastTrackedObjects; // The last objects the LiDar tracked
    private HashMap<Integer, LinkedList<TrackedObjectsEvent>> event_hash = null;


    public LiDarWorkerTracker(int id, int frequency)
    {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.DOWN;
        event_hash = new HashMap<>();
    }

    public void updateLastTrackedObjects(List<TrackedObject> list)
    {
        lastTrackedObjects = list;
    }

    public int getId()
    {
        return id;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS stat)
    {
        this.status = stat;
    }


    // Adding to hashmap the events, storing the events by LinkedLists
    public void addToHash(TrackedObjectsEvent trackedObjectsEvent, int time) {

        event_hash.putIfAbsent(time + frequency, new LinkedList<TrackedObjectsEvent>());
        event_hash.get(time + frequency).add(trackedObjectsEvent);
    }

    public LinkedList<TrackedObjectsEvent> getHashedEvents(int time)
    {
        return event_hash.get(time);
    }


    public void removeHashedEvents(int currentTick) {
        event_hash.remove(currentTick);
    }

    public String toString() {
        return "LiDarWorkerTracker {" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                '}';
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }
}
