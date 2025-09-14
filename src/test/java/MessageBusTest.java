import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;

public class MessageBusTest {
    @Test
    public void unRegisterTest() throws InterruptedException    {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        MicroService testService = new MicroService("TestService") {
            @Override
            protected void initialize() {
                messageBus.register(this);
                subscribeBroadcast(TickBroadcast.class, e->{

                });
                assertTrue(messageBus.isServiceRegisterd(this));// check that it is indeed registerd
                assertTrue(messageBus.isServiceSubbedBC(this,new TickBroadcast(1)));//check that service is subscribed to tick broadcast
                messageBus.unregister(this);
                latch.countDown();
            }
        };
        Thread t = new Thread(testService);
        t.start();
        latch.await();
        assertFalse(messageBus.isServiceRegisterd(testService));
        assertFalse(messageBus.isServiceSubbedBC(testService, new TickBroadcast(1)));
        
    }
    @Test
    public void registerTest() throws InterruptedException{
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        MicroService testService = new MicroService("TestService") {
            @Override
            protected void initialize() {
                messageBus.register(this);
                latch.countDown();
            }
        };
        Thread t = new Thread(testService);
        t.start();
        latch.await();
        assertTrue(messageBus.isServiceRegisterd(testService));
        //checks if the microservice's queue is added to the serviceToQueue map, the queue associated with the microservice is not null and the queue is of the correct type (LinkedBlockingDeque<Message>)
    }
    @Test
    public void subscribeBroadcastTest() throws InterruptedException{
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        TickBroadcast t = new TickBroadcast(1);
        CountDownLatch latch = new CountDownLatch(1);
        MicroService testService = new MicroService("TestService") {
            @Override
            protected void initialize() {
                messageBus.register(this);
                subscribeBroadcast(TickBroadcast.class, e->{

                });
                latch.countDown();
            }
        };
        Thread f = new Thread(testService);
        f.start();
        latch.await();
        assertTrue(messageBus.isServiceSubbedBC(testService, t));//tests if the queue for this message is not null and if the service is containted in this queue
    }
}
