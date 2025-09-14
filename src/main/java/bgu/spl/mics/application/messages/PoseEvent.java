package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;
public class PoseEvent implements Event<Pose> {
    Pose p; 
    public PoseEvent(Pose current){
        this.p = current; 
    }
    public Pose getPose(){
        return p;
    }
}
