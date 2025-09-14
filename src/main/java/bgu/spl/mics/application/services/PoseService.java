package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServiceTracker;


/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    private GPSIMU gpsimu;
    private Pose current;

    public PoseService(GPSIMU gpsimu) {
        super("poseService");
        this.gpsimu = gpsimu;

    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        MessageBusImpl instance = MessageBusImpl.getInstance();
        instance.register(this);

        //update services count
        ServiceTracker s = ServiceTracker.getInstance();
        s.incNumServicesActive();

        //subscriptions
        this.subscribeBroadcast(TickBroadcast.class,tickBroadcast->{
            //sends pose information every tick
            if(gpsimu.getStatus() == STATUS.UP){//length check handeld in gpsimu class
                int tick = tickBroadcast.getCurrentTick();
                gpsimu.setCurrentTick(tick);
                current = gpsimu.getCurrentPose();
                if(current!=null)//if there is a pose at that tick.
                    this.sendEvent(new PoseEvent(current));
            }
            else if(gpsimu.getStatus()==STATUS.DOWN) {
                this.sendBroadcast(new TerminatedBroadcast("GPSIMU DOWN"));
                terminate();
                instance.unregister(this);
            }
            else{
                this.sendBroadcast(new CrashedBroadcast("gpsimu","From PoseService, GPSIMU ERROR"));
                terminate();
                instance.unregister(this);
            }
        });

        this.subscribeBroadcast(CrashedBroadcast.class,message->{
            if (gpsimu.getStatus() != STATUS.ERROR)
            {
                gpsimu.setStatus(STATUS.DOWN);
            }
            sendBroadcast(new TerminatedBroadcast("GPSIMU down due to crashedBroadcast"));
            this.terminate();
            MessageBusImpl.getInstance().unregister(this);

        });

        this.subscribeBroadcast(TerminatedBroadcast.class,message->{
            //incase of system end
            if(message.getTerminationDescription().equals("SYSTEM END")){
                terminate();
                instance.unregister(this);
            }
        });


        GurionRockRunner.latch.countDown();
    }
}
