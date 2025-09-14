 package bgu.spl.mics.application.objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServiceTracker is a singleton class responsible for counting active services
 */
public class ServiceTracker {
    //members
    private static class StaticServiceTrackerHolder {
        private static ServiceTracker instance = new ServiceTracker();
    }
    AtomicInteger numServicesActive;//counts all services
    AtomicInteger numSensorsActive;//counts only how many sensors (ie :cameras and lidars) there are
    public ServiceTracker() {
        this.numServicesActive = new AtomicInteger(0);
        this.numSensorsActive = new AtomicInteger(0);
    }
    public static ServiceTracker getInstance(){
        return StaticServiceTrackerHolder.instance;
    }
    public AtomicInteger getNumServicesActive() {
        return numServicesActive;
    }
    public void incNumServicesActive(){//every service (except fusionSlam) at its init method will call this method to track how many there are 
        numServicesActive.incrementAndGet();
    }
    public void decNumServicesActive(){//in fusion slam we will decrease the num for every terminated broadcast until we reach 0 and end the program
        numServicesActive.decrementAndGet();
    }
    public void incSensoresActive(){
        numSensorsActive.incrementAndGet();
    }
    public void decSensorsActive(){
        numSensorsActive.decrementAndGet();
    }
    public AtomicInteger getSensorsActive(){
        return numSensorsActive;
    }
}