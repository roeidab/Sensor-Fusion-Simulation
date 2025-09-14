import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CameraTest {

    // prepareData is the method that preparing data for the camereService to handle

    //Test regular use with frequency 0
    @Test
    void detectionTest1() {
        ArrayList<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();

        DetectedObject wall = new DetectedObject("Wall_1", "PinkWall");
        DetectedObject chair_1 = new DetectedObject("Chair_1", "Computer Chair");
        DetectedObject chair_2 = new DetectedObject("Chair_2", "Student class chair");

        ArrayList<DetectedObject> detectedObjects_1 = new ArrayList<>();
        detectedObjects_1.add(wall);
        detectedObjects_1.add(chair_1);

        ArrayList<DetectedObject> detectedObjects_2 = new ArrayList<>();
        detectedObjects_2.add(wall);
        detectedObjects_2.add(chair_2);

        detectedObjectsList.add(new StampedDetectedObjects(1, detectedObjects_1));
        detectedObjectsList.add(new StampedDetectedObjects(2, detectedObjects_2));

        Camera camera = new Camera(1, 0, detectedObjectsList);
        camera.setStatus(STATUS.UP);

        StampedDetectedObjects stamped = camera.prepareData(1);
        List<DetectedObject> det_objs = stamped.getList();
        assertTrue(det_objs.contains(wall));
        assertTrue(det_objs.contains(chair_1));
        assertFalse(det_objs.contains(chair_2));

        stamped = camera.prepareData(2);
        List<DetectedObject> det_objs_2 = stamped.getList();
        assertTrue(det_objs_2.contains(wall));
        assertTrue(det_objs_2.contains(chair_2));
        assertFalse(det_objs_2.contains(chair_1));
    }


    //Test regular using camera with frequency 1
    @Test
    void detectionTest2() {
        ArrayList<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();

        DetectedObject wall = new DetectedObject("Wall_1", "PinkWall");
        DetectedObject chair_1 = new DetectedObject("Chair_1", "Computer Chair");
        DetectedObject chair_2 = new DetectedObject("Chair_2", "Student class chair");

        ArrayList<DetectedObject> detectedObjects_1 = new ArrayList<>();
        detectedObjects_1.add(wall);
        detectedObjects_1.add(chair_1);

        ArrayList<DetectedObject> detectedObjects_2 = new ArrayList<>();
        detectedObjects_2.add(wall);
        detectedObjects_2.add(chair_2);

        detectedObjectsList.add(new StampedDetectedObjects(1, detectedObjects_1));
        detectedObjectsList.add(new StampedDetectedObjects(2, detectedObjects_2));

        Camera camera = new Camera(1, 1, detectedObjectsList);
        camera.setStatus(STATUS.UP);

        StampedDetectedObjects stamped = camera.prepareData(1);
        // Camera frequency delay is 1, should return stamped objects only in tick=2, null means nothing detected
        assertNull(stamped);

        stamped = camera.prepareData(2);
        List<DetectedObject> det_objs_2 = stamped.getList();
        assertTrue(det_objs_2.contains(wall));
        assertTrue(det_objs_2.contains(chair_1));
        assertFalse(det_objs_2.contains(chair_2));

        stamped = camera.prepareData(3);
        List<DetectedObject> det_objs_3 = stamped.getList();
        assertTrue(det_objs_3.contains(wall));
        assertTrue(det_objs_3.contains(chair_2));
        assertFalse(det_objs_3.contains(chair_1));
    }


    //Testing if the camera identifies object with error and shutting down
    @Test
    void detectionTest3() {
        ArrayList<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();

        DetectedObject wall = new DetectedObject("Wall_1", "Pink Wall");
        DetectedObject chair_1 = new DetectedObject("Chair_1", "Computer Chair");
        DetectedObject chair_2_E = new DetectedObject("ERROR", "BROKEN CHAIR CAN'T SIT");

        ArrayList<DetectedObject> detectedObjects_1 = new ArrayList<>();
        detectedObjects_1.add(wall);
        detectedObjects_1.add(chair_1);

        ArrayList<DetectedObject> detectedObjects_2 = new ArrayList<>();
        detectedObjects_2.add(wall);
        detectedObjects_2.add(chair_2_E);

        detectedObjectsList.add(new StampedDetectedObjects(1, detectedObjects_1));
        detectedObjectsList.add(new StampedDetectedObjects(2, detectedObjects_2));

        Camera camera = new Camera(1, 0, detectedObjectsList);
        camera.setStatus(STATUS.UP);

        StampedDetectedObjects stamped = camera.prepareData(1);
        List<DetectedObject> det_objs_1 = stamped.getList();
        assertSame(camera.getStatus(), STATUS.UP);
        assertTrue(det_objs_1.contains(wall));
        assertFalse(det_objs_1.contains(chair_2_E));

        stamped = camera.prepareData(2);
        List<DetectedObject> det_objs_2 = stamped.getList();
        assertTrue(det_objs_2.contains(chair_2_E));
        assertSame(camera.getStatus(), STATUS.ERROR);

    }

}
