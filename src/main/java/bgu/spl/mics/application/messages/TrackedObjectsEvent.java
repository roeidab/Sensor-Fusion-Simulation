package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {
    private int tick;
    private List<TrackedObject> lst;
    public TrackedObjectsEvent(List<TrackedObject> list, int tick){
        this.tick = tick;
        this.lst = list;
    }
    public int getTick(){
        return tick;
    }
    public List<TrackedObject> getTrackedObjects(){
        return lst;
    }
}
