package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick; // the current time
    private List<Pose> poseList; // Represents a list of time-stamped poses
    private STATUS status; // enum: up/down/error
    
    public GPSIMU(int currentTick,List<Pose> poseList, STATUS status){
        this.currentTick = currentTick;
        this.poseList = poseList;
        this.status = status;
    }

    public Pose getCurrentPose(){
        if(currentTick>=poseList.size()){
            status = STATUS.DOWN;
            return null;
        }
        return poseList.get(currentTick-1);
    }
    public void currentTickInc(){
        this.currentTick++;
    }
    public Pose getPoseAtTick(int tick){
        if(currentTick>=poseList.size()){
            status = STATUS.DOWN;
            return null;
        }
        return this.poseList.get(tick);
    }
    public void setCurrentTick(int tick){
        currentTick = tick;
    }

    public STATUS getStatus(){
        return this.status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
