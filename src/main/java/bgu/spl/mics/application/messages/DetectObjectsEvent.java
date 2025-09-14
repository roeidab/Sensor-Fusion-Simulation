package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class DetectObjectsEvent implements Event<Boolean> {
    private StampedDetectedObjects stampedObjects;

    public DetectObjectsEvent(StampedDetectedObjects stampedObj){
        this.stampedObjects = stampedObj;
    }

    public StampedDetectedObjects getObjects()
    {
        return stampedObjects;
    }


}
