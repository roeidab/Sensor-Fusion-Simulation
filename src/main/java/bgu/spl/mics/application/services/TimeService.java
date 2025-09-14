package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ServiceTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private final int tick_Time;
    private final int total_Ticks;
    private int currentTick;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tick_Time = TickTime;
        this.total_Ticks = Duration;
        this.currentTick = 1;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        //register to messegeBus
        MessageBusImpl instance = MessageBusImpl.getInstance();
        instance.register(this);
        
        //update services count
        ServiceTracker s = ServiceTracker.getInstance();
        s.incNumServicesActive();

        //subscriptions
        subscribeBroadcast(CrashedBroadcast.class, term -> {
            sendBroadcast(new TerminatedBroadcast("TimeService down due to crashedBroadcast"));
            this.terminate();
            instance.unregister(this);
        });

        subscribeBroadcast(TerminatedBroadcast.class, term -> {
            //in case of system end
            if(term.getTerminationDescription().equals("SYSTEM END")){
                terminate();
                instance.unregister(this);
            }

        });
        // Start the tick broadcasting
        subscribeBroadcast(TickBroadcast.class, tb -> {
            try {
                if ( currentTick < total_Ticks)
                {
                    currentTick++;
                    StatisticalFolder.getInstance().incSystemRunTime();
                    Thread.sleep(tick_Time*1000); // Sleep for the duration of one tick, in seconds
                    sendBroadcast(new TickBroadcast(currentTick));
                }
                else {
                    terminate();
                    // After broadcasting all ticks, send a TerminatedBroadcast - Not sure check if needed
                    sendBroadcast(new TerminatedBroadcast("SYSTEM END"));
                    MessageBusImpl.getInstance().unregister(this);
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();// Handle interrupt
            }
        });
        sendBroadcast(new TickBroadcast(currentTick));
        StatisticalFolder.getInstance().incSystemRunTime();
    }
}
