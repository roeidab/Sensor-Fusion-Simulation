package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;

import bgu.spl.mics.application.objects.*;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("cameraService " + camera.getID());
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        MessageBusImpl instance = MessageBusImpl.getInstance();
        camera.setStatus(STATUS.UP); // Turning on the camera
        instance.register(this);

        //update services count
        ServiceTracker s = ServiceTracker.getInstance();
        s.incNumServicesActive();

        this.subscribeBroadcast(TerminatedBroadcast.class, message->{
            if(message.getTerminationDescription().equals("SYSTEM END")){
                terminate();
                sendBroadcast(new TerminatedBroadcast("Camera "+camera.getID()+" down due to SYSTEM END"));
                instance.unregister(this);
            }
        });

        this.subscribeBroadcast(CrashedBroadcast.class,message->{
            if (camera.getStatus() != STATUS.ERROR)
            {
                camera.setStatus(STATUS.DOWN);
            }
            sendBroadcast(new TerminatedBroadcast("Camera "+camera.getID()+" down due to crashedBroadcast"));
            this.terminate();
            MessageBusImpl.getInstance().unregister(this);
            //Add to last frames
            LastFrame f = LastFrame.getInstance();
            f.addLastCamerasFrame("Camera "+camera.getID(), camera.LastDetectedObjects());
        });

        this.subscribeBroadcast(TickBroadcast.class, tickBroadcast->{
            StampedDetectedObjects objs = camera.prepareData(tickBroadcast.getCurrentTick());
            if (this.camera.getStatus() == STATUS.ERROR  )
            {
                LastFrame f = LastFrame.getInstance();
                f.addLastCamerasFrame("Camera " + camera.getID(), camera.LastDetectedObjects());
                this.terminate();
                MessageBusImpl.getInstance().unregister(this);
                sendBroadcast(new CrashedBroadcast("Camera " + camera.getID(), camera.getErrorDescription()));
            }
            else if (this.camera.getStatus() == STATUS.UP  && objs != null) // Status UP and objs is null means no detected objects
            {
                StatisticalFolder.getInstance().incNumDetectedObjects(objs.getList().size());
                sendEvent(new DetectObjectsEvent(objs));
            }
            else if (this.camera.getStatus() == STATUS.DOWN )
            {
                this.terminate();
                MessageBusImpl.getInstance().unregister(this);
                sendBroadcast(new TerminatedBroadcast("Camera "+camera.getID()+" down"));
            }
        });
        GurionRockRunner.latch.countDown();
    }
}
