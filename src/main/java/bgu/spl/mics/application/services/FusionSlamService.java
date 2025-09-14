package bgu.spl.mics.application.services;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LastFrame;
import bgu.spl.mics.application.objects.ServiceTracker;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.CloudPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.objects.StatisticalFolder;
/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    
    FusionSlam fusionIns;
    StatisticalFolder sf;
    //To handle cases where poseevent arrives after tracked event
    private Map<Integer,TrackedObjectsEvent> waitingTrackedObjectEvents;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        fusionIns = fusionSlam;
        sf = StatisticalFolder.getInstance();
        this.waitingTrackedObjectEvents = new HashMap<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {

        MessageBusImpl instance = MessageBusImpl.getInstance();
        instance.register(this);
        //Service tracker
        ServiceTracker s = ServiceTracker.getInstance();
        //subscriptions
        subscribeEvent(PoseEvent.class, poseE -> {
            fusionIns.addPose(poseE.getPose());//added sequentiallly by the nature of pose event happening every tick
            //check if there is a tracked event waiting for this pose event
            TrackedObjectsEvent trackedObjectsEvent = waitingTrackedObjectEvents.get(poseE.getPose().getTick());
            if (trackedObjectsEvent != null) {
                processTrackedObjectsEvent(trackedObjectsEvent);
                waitingTrackedObjectEvents.remove(poseE.getPose().getTick()); // Remove from buffer
            }
        });
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectEvent -> {
            if(fusionIns.getPoses().size()>=trackedObjectEvent.getTick())
                processTrackedObjectsEvent(trackedObjectEvent);// Process immediately if PoseEvent is already available
            else
                waitingTrackedObjectEvents.put(trackedObjectEvent.getTick(), trackedObjectEvent);// Otherwise, wait for the event
        });
        
        subscribeBroadcast(TerminatedBroadcast.class, tecrm -> {
            s.decNumServicesActive();
            if(s.getNumServicesActive().get()<=2){//only checks if all sensors are done ie: lidar and camera only, as they terminate when finishing their work
                sendBroadcast(new TerminatedBroadcast("SYSTEM END"));
                terminate();
                
                instance.unregister(this);
                generateOutputFile(false);
            }

        });
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            terminate();
            instance.unregister(this);
            LastFrame f = LastFrame.getInstance();
            f.setError(crashed.getErrorDescription());
            f.setFaultySensor(crashed.getSensor());
            f.setPoses(fusionIns.getPoses());
            generateOutputFile(true);
        });
        GurionRockRunner.latch.countDown();
    }

    private void generateOutputFile(Boolean error){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<LandMark> landmarks = fusionIns.getLandmarks();
        sf.setLandmarks(landmarks);
        File inputFile = new File(GurionRockRunner.filePath);
        try{
            FileWriter fileWriter;
            if(!error){
                File outPutFile = new File(inputFile.getParent(),"output_file.json");
                fileWriter = new FileWriter(outPutFile);
                gson.toJson(sf,fileWriter);
            }
            else{//if crashed
                sf.decSystemTime();
                File outPutFile = new File(inputFile.getParent(),"error_output.json");
                fileWriter = new FileWriter(outPutFile);
                gson.toJson(LastFrame.getInstance(),fileWriter);
            }
            fileWriter.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    private void processTrackedObjectsEvent(TrackedObjectsEvent trackedObjectEvent){
        for ( TrackedObject t : trackedObjectEvent.getTrackedObjects()) {
            int i = fusionIns.updateMap(t, trackedObjectEvent.getTick());//add to map
            if(i==1){
                sf.incNumLandmarks();
            }
            /*if(i==-1){
                //should never reach here
            }*/
        }
    }
}
