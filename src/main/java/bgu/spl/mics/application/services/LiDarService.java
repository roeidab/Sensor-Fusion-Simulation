package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    private final LiDarWorkerTracker workerTracker;
    private int lastTick;
    private int maxTrackedObjects = -1;

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Lidar Worker Tracker " + LiDarWorkerTracker.getId() );
        workerTracker = LiDarWorkerTracker;
        lastTick = 0;
        if (LiDarDataBase.getInstance() != null) {
            maxTrackedObjects = LiDarDataBase.getInstance().getCloudPoints().size();
        }
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        workerTracker.setStatus(STATUS.UP);
        MessageBusImpl instance = MessageBusImpl.getInstance();
        instance.register(this);

        //update services count
        ServiceTracker s = ServiceTracker.getInstance();
        s.incNumServicesActive();

        //subscriptions
        this.subscribeEvent(DetectObjectsEvent.class, message->{
            if ( workerTracker.getStatus() == STATUS.UP)
            {
                /*1. Mekabel stampedObject, 2. Moze oto bDatabase 3. Mekabel et a PC shelo 4. Mavir le FusionSlam*/
                StampedDetectedObjects stamped_objList = message.getObjects();
                /* Once done proccessing push the event to hashmap by key=time+freq*/
                ArrayList<TrackedObject> trackedObjects = this.getTrackedObjects_by_id(stamped_objList);

                if (trackedObjects != null && !trackedObjects.isEmpty() )
                {
                    if (stamped_objList.getTime() + workerTracker.getFrequency() <= lastTick ) // Send now
                    {
                        StatisticalFolder.getInstance().incNumTrackedObjects(trackedObjects.size());
                        workerTracker.updateLastTrackedObjects(trackedObjects);
                        sendEvent(new TrackedObjectsEvent(trackedObjects, stamped_objList.getTime()));
                        MessageBusImpl.getInstance().complete(message,true); // Returns true to the messageBus
                    }
                    else { // Send the event later
                        workerTracker.addToHash(new TrackedObjectsEvent(trackedObjects, stamped_objList.getTime()), stamped_objList.getTime());
                    }

                }
                complete(message, true);
            }
            else
            {
                // do nothing
            }

        });

        // Goes over the map and sending events that are ready to be sent at the time
        this.subscribeBroadcast(TickBroadcast.class, message->{
            lastTick = message.getCurrentTick();

            if (/* workerTracker.getStatus() == STATUS.UP && */
                    StatisticalFolder.getInstance().getNumTrackedObjects().get() < maxTrackedObjects)
            {
                LinkedList<TrackedObjectsEvent> trackedEvents = workerTracker.getHashedEvents(message.getCurrentTick());
                if (trackedEvents != null )
                {
                    for (TrackedObjectsEvent event: trackedEvents)
                    {
                        StatisticalFolder.getInstance().incNumTrackedObjects(event.getTrackedObjects().size());
                        workerTracker.updateLastTrackedObjects(event.getTrackedObjects());
                        sendEvent(event);
                        MessageBusImpl.getInstance().complete(event,true); // Returns true to the messageBus
                    }
                    workerTracker.removeHashedEvents(message.getCurrentTick()); // Delete from the map after sending events
                }
            }
            else {
                instance.unregister(this);
               terminate();
               sendBroadcast(new TerminatedBroadcast(
                       "*** LiDar terminating, there's no more objects to track ***"));
            }
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, message->{
            if(message.getTerminationDescription().equals("SYSTEM END")){
                sendBroadcast(new TerminatedBroadcast(
                        "LiDar "+ workerTracker.getId()+" Shutting down and terminating due to SYSTEM END"));
                terminate();
                instance.unregister(this);
            }
        });
        this.subscribeBroadcast(CrashedBroadcast.class, message->{
            if (workerTracker.getStatus() != STATUS.ERROR)
            {
                workerTracker.setStatus(STATUS.DOWN);
                instance.unregister(this);
                terminate();
                sendBroadcast(new TerminatedBroadcast(
                        "LiDar "+ workerTracker.getId()+" Shutting down and terminating due to crashedBroadcast"));
                //Add to last frames
                LastFrame f = LastFrame.getInstance();
                f.addLastLiDarWorkerTrackersFrame(getName(), workerTracker.getLastTrackedObjects());
            }
        });

        GurionRockRunner.latch.countDown();
    }

    // This function will find the objects cloudPoints
    /*
    *  @param objects_to_find - the stamped objects that the camera sent
    * */
    private ArrayList<TrackedObject> getTrackedObjects_by_id(StampedDetectedObjects objects_to_find) {
        // Gets all the cloud points from DB
        ArrayList<StampedCloudPoints> cloudPoints = LiDarDataBase.getInstance().getCloudPoints();

        int i = 0;
        boolean foundTime = false;
        ArrayList<TrackedObject> trackedObjects = new ArrayList<>();

        // Find the index for the matching time in the DB
        while (!foundTime && i < cloudPoints.size())
        {
            if (cloudPoints.get(i).getTime() == objects_to_find.getTime() )
            {
                foundTime = true;
            }
            else
            {
                i = i + 1;
            }
        }

        // Searching all the objects cloudPoints starting from i
        for (int k = i; k < cloudPoints.size() && foundTime; k++ )
        {
            if (cloudPoints.get(k).getTime() != objects_to_find.getTime()) // Reached over the needed time
            {
                foundTime = false; // Stop iterating
            }
            else {
                // Check if this object is in the list from the camera
                for (DetectedObject obj : objects_to_find.getList() )
                {
                    if (obj.getId().equals("ERROR") )
                    {
                        sendBroadcast(new CrashedBroadcast("LiDar "+workerTracker.getId(),"Crashed: LiDarTrackerWorker " + workerTracker.getId()
                                + " Received errorID " + obj.getDescription()));
                        workerTracker.setStatus(STATUS.ERROR);
                        terminate();
                        MessageBusImpl.getInstance().unregister(this);
                        //Add to last frames
                        LastFrame f = LastFrame.getInstance();
                        f.addLastLiDarWorkerTrackersFrame(getName(), workerTracker.getLastTrackedObjects());
                        return null;
                    }

                    if ( cloudPoints.get(k).getId().equals(obj.getId()))
                    {
                        trackedObjects.add(new TrackedObject(objects_to_find.getTime(), obj.getId(),
                                obj.getDescription(),cloudPoints.get(k).getCloudPoints()));
                    }
                }
            }
        }
        return trackedObjects;

    }
}
