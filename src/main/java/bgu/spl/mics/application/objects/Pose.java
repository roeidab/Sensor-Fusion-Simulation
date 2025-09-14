package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private float x;
    private float y;
    private float yaw; // The orientation angle relative to the charging station's coordinate system
    private int time; // The time when the robot reaches the pose;
    
    public Pose(float x, float y,float yaw, int time){
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public float getYaw(){
        return this.yaw;
    }
    public int getTick(){
        return this.time;
    }
}
