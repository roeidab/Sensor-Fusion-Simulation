package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigurationParser {

    private int tickTime;
    private int duration;
    private ArrayList<Camera> cameras;
    private ArrayList<LiDarWorkerTracker> lidarWorkers;
    private GPSIMU gpsimu;

    public ConfigurationParser(String configFilePath) throws IOException {
        parseConfiguration(configFilePath);
    }

    private void parseConfiguration(String configFilePath) throws IOException {
        try (FileReader reader = new FileReader(configFilePath)) {
            Gson gson = new Gson();

            // Parse the configuration file
            Type configType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> configData = gson.fromJson(reader, configType);

            // Extract TickTime and Duration
            this.tickTime = ((Double) configData.get("TickTime")).intValue();
            this.duration = ((Double) configData.get("Duration")).intValue();

            // Resolve base directory
            File configFile = new File(configFilePath);
            String baseDir = configFile.getParent();

            // Extract Cameras
            Map<String, Object> camerasConfig = (Map<String, Object>) configData.get("Cameras");
            String cameraDataPath = new File(baseDir, (String) camerasConfig.get("camera_datas_path")).getCanonicalPath();
            List<Map<String, Object>> cameraList = (List<Map<String, Object>>) camerasConfig.get("CamerasConfigurations");

            cameras = new ArrayList<>();
            CameraDataParser cameraDataParser = new CameraDataParser();
            List<List<StampedDetectedObjects>> parsedCameraData = cameraDataParser.parseCameraData(cameraDataPath);

            int index = 0;
            for (Map<String, Object> cameraConfig : cameraList) {
                int id = ((Double) cameraConfig.get("id")).intValue();
                int frequency = ((Double) cameraConfig.get("frequency")).intValue();

                // Assign parsed detected objects to each camera
                cameras.add(new Camera(id, frequency, new ArrayList<>(parsedCameraData.get(index++))));
            }

            // Extract LiDAR Workers
            Map<String, Object> lidarConfig = (Map<String, Object>) configData.get("LiDarWorkers");
            String lidarDataPath = new File(baseDir, (String) lidarConfig.get("lidars_data_path")).getCanonicalPath();
            List<Map<String, Object>> lidarList = (List<Map<String, Object>>) lidarConfig.get("LidarConfigurations");

            LiDarDataBase.getInstance(lidarDataPath); // Creates first instance of LidarDB and parse everything inside
            lidarWorkers = new ArrayList<>();
            for (Map<String, Object> lidarWorkerConfig : lidarList) {
                int id = ((Double) lidarWorkerConfig.get("id")).intValue();
                int frequency = ((Double) lidarWorkerConfig.get("frequency")).intValue();

                // Create a LiDarWorkerTracker object
                lidarWorkers.add(new LiDarWorkerTracker(id, frequency));
            }

            // Resolve pose data path
            String poseDataPath = new File(baseDir, (String) configData.get("poseJsonFile")).getCanonicalPath();

            // Parse Pose Data
            PoseDataParser poseDataParser = new PoseDataParser();
            List<Pose> poseList = poseDataParser.parsePoseData(poseDataPath);

            // Initialize GPSIMU with the parsed pose list
            this.gpsimu = new GPSIMU(0, poseList, STATUS.UP);
        }
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }

    public ArrayList<Camera> getCameras() {
        return cameras;
    }

    public ArrayList<LiDarWorkerTracker> getLidarWorkers() {
        return lidarWorkers;
    }

    public GPSIMU getGpsimu() {
        return gpsimu;
    }
}
