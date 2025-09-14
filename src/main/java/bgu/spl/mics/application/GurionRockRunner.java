package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static CountDownLatch latch;
    public static String filePath;
    public static void main(String[] args) {
        filePath = args[0];
        //String filePath = "example input\\configuration_file.json"; // Replace with actual path from args[0]
        try {
            ConfigurationParser parser = new ConfigurationParser(new File(filePath).getPath());
            int tickTime = parser.getTickTime();
            int duration = parser.getDuration();
            List<Camera> camerasList = parser.getCameras();
            List<LiDarWorkerTracker> lidarWorkers = parser.getLidarWorkers();
            GPSIMU gpsimu = parser.getGpsimu();

            // Initialize CountDownLatch
            int totalServices = camerasList.size() + lidarWorkers.size() + 2; // +2 for PoseService and FusionSlamService
            latch = new CountDownLatch(totalServices);

            // Start Camera Services
            for (Camera cam : camerasList) {
                CameraService cameraServ = new CameraService(cam);
                Thread t = new Thread(cameraServ);
                t.start();
            }

            // Start LiDAR Services
            // Initilaizing the LiDarDB with path in the configurationParser just before creating workers
            for (LiDarWorkerTracker lidarWorker : lidarWorkers) {
                LiDarService lidarServ = new LiDarService(lidarWorker);
                Thread t = new Thread(lidarServ);
                t.start();
            }

            // Start Pose Service
            PoseService poseSer = new PoseService(gpsimu);
            Thread t = new Thread(poseSer);
            t.start();

            // Start FusionSlam Service
            FusionSlamService fusServ = new FusionSlamService(FusionSlam.getInstance());
            Thread f = new Thread(fusServ);
            f.start();

            // Wait for all services to initialize before running TimeService
            latch.await();
            TimeService timeServ = new TimeService(tickTime, duration);
            Thread h = new Thread(timeServ);
            h.start();
            h.join(); // wait for the timeSer to finish
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}












