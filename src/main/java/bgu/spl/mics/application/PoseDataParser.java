package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.Pose;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class PoseDataParser {

    public List<Pose> parsePoseData(String poseDataPath) throws IOException {
        try (FileReader reader = new FileReader(poseDataPath)) {
            Gson gson = new Gson();

            // Define the type for deserialization
            Type listType = new TypeToken<List<Pose>>() {}.getType();

            // Deserialize and return the list of Pose objects
            return gson.fromJson(reader, listType);
        }
    }
}
