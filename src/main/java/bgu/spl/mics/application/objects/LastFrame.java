package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LastFrame {
    private String error;
    private String faultySensor;
    private ConcurrentHashMap<String,StampedDetectedObjects> lastCameraFrames;
    private ConcurrentHashMap<String,List<TrackedObject>> lastLidarFrames;
    private List<Pose> poses;
    private StatisticalFolder statistics;
    private static class FrameHolder {
        private static final LastFrame instance = new LastFrame();
    }
    private LastFrame(){
        this.error = "";
        this.faultySensor = "";
        lastCameraFrames = new ConcurrentHashMap<>();
        lastLidarFrames = new ConcurrentHashMap<>();
        poses = null;
        this.statistics = StatisticalFolder.getInstance();
    }
    public static LastFrame getInstance(){
        return FrameHolder.instance;
    }
    public void setError(String error) {
        this.error = error;
    }
    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }
    public void addLastCamerasFrame(String cameraID,StampedDetectedObjects lastCamerasFrame) {
        this.lastCameraFrames.put(cameraID, lastCamerasFrame);
    }
    public void addLastLiDarWorkerTrackersFrame(String lidarID,List<TrackedObject> lastLiDarWorkerTrackersFrame) {
        this.lastLidarFrames.put(lidarID, lastLiDarWorkerTrackersFrame);
    }
    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }
    
}