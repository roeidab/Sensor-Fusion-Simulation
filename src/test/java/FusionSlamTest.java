import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

public class FusionSlamTest {
    private FusionSlam instance = FusionSlam.getInstance();
    @BeforeEach
    public void setUp(){//insure a clean state as it is a singleton
        instance.clearLandmarks();
        instance.clearPoses();
    }
    @Test
    public void addNewLandMarkTest(){
        instance.addPose(new Pose(0, 0, 45, 1));
        instance.addPose(new Pose(1, 1, 90,2));
        ArrayList<CloudPoint> l = new ArrayList<>();
        l.add((new CloudPoint(1, 2)));
        TrackedObject trackedObject1 = new TrackedObject(1,"wall_1","wall",l);
        //call update map
        int result = instance.updateMap(trackedObject1, 1);
        //verify a new landmark was added, and transformed accordingly
        assertEquals(1, result);
        assertEquals(1,instance.getLandmarks().size());
        LandMark addedLandMark = instance.getLandmarks().get(0);
        assertEquals("wall_1", addedLandMark.getId());
        assertEquals("wall", addedLandMark.getDescription());
        assertEquals(1, addedLandMark.getCoordinates().size());
        assertEquals( -Math.sqrt(2)/2, addedLandMark.getCoordinates().get(0).getX(),0.0001);
        assertEquals(3*Math.sqrt(2)/2, addedLandMark.getCoordinates().get(0).getY(),0.0001); // The point should be transformed
    }
    @Test
    public void updateExistingLandMark(){
        instance.addPose(new Pose(2.5f,-3.125f,45,1));
        instance.addPose(new Pose(1, 1, 90, 2));

        ArrayList<CloudPoint> firstCloudPoints = new ArrayList<>();
        firstCloudPoints.add(new CloudPoint(1, 2));
        TrackedObject trackedObject1 = new TrackedObject(1, "wall_1", "wall", firstCloudPoints);
        instance.updateMap(trackedObject1, 1);

        ArrayList<CloudPoint> secondCloudPoints = new ArrayList<>();
        secondCloudPoints.add(new CloudPoint(2, 3));  // New coordinates
        TrackedObject trackedObject2 = new TrackedObject(2, "wall_1", "wall", secondCloudPoints);
        int result = instance.updateMap(trackedObject2, 2);  // Use tick 2 for the second update

        LandMark updatedLandMark = instance.getLandmarks().get(0);
        assertEquals(0, result);  // No new landmark should be added
        assertEquals("wall_1", updatedLandMark.getId());
        assertEquals("wall", updatedLandMark.getDescription());
        assertEquals(1, updatedLandMark.getCoordinates().size());
        //expected coordinates
        double firstx = -Math.sqrt(2)/2 + 2.5;double firsty=3*Math.sqrt(2)/2-3.125;
        double secondx = -3+1; double secondy = 2+1;
        assertEquals((firstx+secondx)/2, updatedLandMark.getCoordinates().get(0).getX(),0.0001);
        assertEquals((firsty+secondy)/2, updatedLandMark.getCoordinates().get(0).getY(),0.0001); 
    }
}
