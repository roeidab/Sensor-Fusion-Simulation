package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private ArrayList<LandMark> landmarks; // Represents the map of the enviroment (switched landmarks to arraylist)
    private List<Pose> poses; // Represents previous poses needed for calculations
    // Singleton instance holder

    private FusionSlam()
    {
        landmarks = new ArrayList<LandMark>(); 
        poses = new ArrayList<Pose>();

    }

    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance(){
        return FusionSlam.FusionSlamHolder.instance;
    }
   
    public void addPose(Pose p){
        poses.add(p);
    }
    public Pose getPoseAt(int index){
        return this.poses.get(index);
    }

    public ArrayList<LandMark> getLandmarks() {
        return landmarks;
    }
    /**
    * @param p A valid TrackedObject.
    * @param tick An int value represting a system tick.
    * @pre 
    * 1 <= tick <= poses.size()
    * p != null && !p.getCloudPoints().isEmpty()
    * poses != null
    * landMarks!=null
    * @post 
    * 1. If isUnique == true (i.e., TrackedObject p is not in landmarks):
    * - this.landmarks.size() == @pre(this.landmarks.size()) + 1  
    * - A new LandMark is added with ID p.getID() and description p.getDesc()  
    * - The coordinates of the new LandMark correspond to the global coordinates of p.getCloudPoints() after transformation using the pose at tick.  
    * @post
    * 2. If isUnique == false (i.e., TrackedObject p is already in landmarks):
    * - this.landmarks.size() == @pre(this.landmarks.size())  
    * - The coordinates of the existing LandMark corresponding to p.getID() are updated by averaging the old and new coordinates.  
    * - The new coordinates for each CloudPoint in the existing LandMark are computed as the average of the old and transformed coordinates.  
    * @post
    * There are no duplicate LandMarks in the map, i.e., all LandMark IDs are unique.  
    * landmarks[index].id = p.id && landmarks[index].description.equals(p.description)
    * 
    * @return 1 if a new landmark was added, 0 otherwise.
    */
    public int updateMap(TrackedObject p, int tick){
        int isUnique = 0;
        int index = getLandMarkIndex(p);
        /*if(poses.size()<tick){
            return -1; 
        }*/
        List<CloudPoint> trackedObjectCPGlobal = new ArrayList<CloudPoint>(); // make a list of the global coordinates of the tracked object
        for(CloudPoint c: p.getCloudPoints()){
            trackedObjectCPGlobal.add(rotateAndCalcGlobal(c, poses.get(tick-1)));//transform coordiantes and to list
        }
        if(index!=-1){//if exists in the map
            //average coordinates, notice the number of sets of xy may be different
            List<CloudPoint> l = averageCP(landmarks.get(index).getCoordinates(), trackedObjectCPGlobal);
            landmarks.set(index, new LandMark(p.getID(), p.getDesc(),l));//replace with new list
        }
        else{//if new object
            landmarks.add(new LandMark(p.getID(), p.getDesc(),trackedObjectCPGlobal)); //add a new landmark to the map
            isUnique = 1;
        }
        return isUnique;
    }
    //private calculation methods
    private CloudPoint rotateAndCalcGlobal(CloudPoint c, Pose p){
        double localX = c.getX() ;double localY=c.getY();//local coordiantes 
        double pX = p.getX();double pY = p.getY();double degreeYaw = p.getYaw();//robot postion
        double radianYaw = degreeYaw*Math.PI/180; // convet to radian
        double cosTheta = Math.cos(radianYaw); double sinTheta = Math.sin(radianYaw);
        double globalX = cosTheta*localX-sinTheta*localY + pX; double globalY = sinTheta*localX+cosTheta*localY +pY; //use transformation     
        return new CloudPoint(globalX,globalY);
    }
    private List<CloudPoint> averageCP(List<CloudPoint> existing, List<CloudPoint> newList){
        int index = 0;
        List<CloudPoint> res = new ArrayList<>();
        while(index<existing.size()&&index<newList.size()){
            double newX = (existing.get(index).getX() + newList.get(index).getX())/2;//average
            double newY = (existing.get(index).getY() + newList.get(index).getY())/2;
            res.add(new CloudPoint(newX,newY));
            index++;
        }//incase one the lists is bigger we will check sepratley
        for(int i = index; i<existing.size();i++){// will only enter if there are elemets left only in the old list
            res.add(existing.get(i));
        }
        for(int i = index;i<newList.size();i++){//ditto for the array
            res.add(newList.get(i));
        }
        return res;
    }
    private int getLandMarkIndex(TrackedObject t){//returns index of landmark with matching id
        for(int i = 0;i<landmarks.size();i++){
            if(landmarks.get(i).getId().equals(t.getID())){//if already exists
                return i;
            }
        }
        return -1;//if new object, ie: not in landmarks
    }

    public List<Pose> getPoses() {
        return poses;
    }

    // Reset methods for tests
    public void clearLandmarks() {
        landmarks.clear();
    }

    public void clearPoses() {
        poses.clear();
    }
}
