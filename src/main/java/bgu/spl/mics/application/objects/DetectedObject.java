package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {

    //members
    private String id;
    private String description;

    //methods
    public DetectedObject(String id, String desc){
        this.id = id;
        description = desc;
    }

    public String getDescription() {
        return description;
    }
    public String getId(){ return id; }
}
