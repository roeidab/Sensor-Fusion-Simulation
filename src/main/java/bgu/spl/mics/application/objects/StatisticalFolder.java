package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private final AtomicInteger systemRunTime; // The total runtime of the system, measured in ticks
    private final AtomicInteger numDetectedObjects; /* The cumulative count of objects detected by all cameras - includes both initial
    detections and subsequent re-detections*/
    private final AtomicInteger numTrackedObjects; /* Cumulative counts of objects tracked by all LiDars, encompassing both new and
     ongoing tracking of previously detected objects */
    private final AtomicInteger numLandmarks; /* The total number of unique landmarks identified and mapped. Updated only when
     new landmarks are added to the map */
    private List<LandMark> landmarks;
    public void setLandmarks(List<LandMark> landmarks) {
        this.landmarks = landmarks;
    }

    private static class StatisticalHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    public static StatisticalFolder getInstance() {
        return StatisticalHolder.instance;
    }

    private StatisticalFolder() {
        this.systemRunTime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
        this.landmarks = null; // will only get a value when the program is terminated
    }

    public void incSystemRunTime() {
        this.systemRunTime.incrementAndGet();
    }

    public void incNumDetectedObjects(int numDetectedObjects) {
        this.numDetectedObjects.addAndGet(numDetectedObjects);
    }

    public void incNumTrackedObjects(int numTrackedObjects) {
        this.numTrackedObjects.addAndGet(numTrackedObjects);
    }

    public void incNumLandmarks() {
        this.numLandmarks.incrementAndGet();
    }

    public AtomicInteger getNumTrackedObjects() {
        return numTrackedObjects;
    }
    public void decSystemTime(){
        this.systemRunTime.decrementAndGet();
    }
}
