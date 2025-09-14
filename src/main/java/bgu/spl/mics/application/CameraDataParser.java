package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraDataParser {

    // Parses and returns a list of cameras with their StampedDetectedObjects
    public List<List<StampedDetectedObjects>> parseCameraData(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();

            // Define the type for deserialization
            Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();

            // Deserialize the parsed data into a map
            Map<String, List<StampedDetectedObjects>> cameraMap = gson.fromJson(reader, type);

            // Convert the map to a list of lists
            return new ArrayList<>(cameraMap.values());
        }
    }


//     try {
//        CameraDataParser parser = new CameraDataParser();
//        List<List<StampedDetectedObjects>> cameras = parser.parseCameraData(filePath);
//
//        // Example: Iterate and process data by camera index
//        for (int i = 0; i < cameras.size(); i++) {
//            System.out.println("Camera " + (i + 1) + ":");
//            for (StampedDetectedObjects data : cameras.get(i)) {
//                processStampedDetectedObjects(data);
//            }
//        }
//
//    } catch (IOException e) {
//        e.printStackTrace();
//    }


    /*private static void processStampedDetectedObjects(StampedDetectedObjects data) {
        System.out.println("  Time: " + data.getTime());
        for (DetectedObject obj : data.getList()) {
            System.out.println("    Object ID: " + obj.getId() + ", Description: " + obj.getDescription());
        }
    }*/
}

