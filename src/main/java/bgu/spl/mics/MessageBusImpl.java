package bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private static class MessageBusHolder{
		private static final MessageBusImpl instance = new MessageBusImpl();
	}
	//members
	private ConcurrentHashMap<MicroService,LinkedBlockingDeque<Message>> serviceToQueue;
	private ConcurrentHashMap<Class<? extends Message>,ConcurrentLinkedQueue<MicroService>> messageToService;
	private ConcurrentHashMap<Event<?>,Future<?>> eventToFuture;
	//Lock
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
	//functions
	private MessageBusImpl(){//constructor
		serviceToQueue = new ConcurrentHashMap<MicroService,LinkedBlockingDeque<Message>>();
		messageToService = new ConcurrentHashMap<Class<? extends Message>,ConcurrentLinkedQueue<MicroService>>();
		eventToFuture = new ConcurrentHashMap<Event<?>,Future<?>>();
	}
	public static MessageBusImpl getInstance(){
		return MessageBusHolder.instance;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m){
		messageToService.computeIfAbsent(type, k->new ConcurrentLinkedQueue<MicroService>());
		messageToService.get(type).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		messageToService.computeIfAbsent(type, k->new ConcurrentLinkedQueue<MicroService>());
		messageToService.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> f = (Future<T>)eventToFuture.get(e);
		f.resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		readLock.lock();
//		System.out.println("ReadLock, sending -" + b.getClass()+  "- Broadcast for every microservice subbed to" );
		ConcurrentLinkedQueue<MicroService> l = messageToService.get(b.getClass());
		if(l!=null){
			for ( MicroService m : l) {//for every microservice subscribed to this message(b) add the message to their working q.
				if(b instanceof CrashedBroadcast || b instanceof TerminatedBroadcast){
					serviceToQueue.get(m).addFirst(b);
				}
				else
					serviceToQueue.get(m).addLast(b);
			}
		}
		readLock.unlock();
//		System.out.println("unLock, finished adding to queue " + b.getClass() + " for every MicroService subbed");
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		writeLock.lock();
//		System.out.println("ReadLock, sending -" + e.getClass()+  "- Event to message" );
		try{
			ConcurrentLinkedQueue<MicroService> microservices = messageToService.get(e.getClass());
			if(microservices!=null&&!microservices.isEmpty()){
				MicroService m = microservices.poll();
//				System.out.println("Added to service"+m.getClass()+"event e:"+e.getClass());
				serviceToQueue.get(m).addLast(e);
				microservices.add(m);
			}
			else return null;
			Future<T> future = new Future<T>();
			eventToFuture.put(e, future);
			return future;
		}
		finally{
			writeLock.unlock();
//			System.out.println("unLock, finished adding to queue " + e.getClass() + " Event to message");
		}
	}

	@Override
	public void register(MicroService m) {
		serviceToQueue.computeIfAbsent(m, k-> new LinkedBlockingDeque<Message>());//if key is not mapped, add a new empty message queue in its place
	}

	@Override
	public void unregister(MicroService m){
		writeLock.lock();
        try {
			//clear and remove its message queue.
			serviceToQueue.get(m).clear();
			serviceToQueue.remove(m);

			//unsubscribe from all events/broadcasts
			for(ConcurrentLinkedQueue<MicroService> queue: messageToService.values()){
				queue.remove(m);
			}
		}	
		finally {
			writeLock.unlock(); // Release write lock
		}
		
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message l = null;
		try{
			l = serviceToQueue.get(m).take();//using take this will wait until there is a message in the q
		}
		catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		return l;
	}

	
	//Queries
	public Boolean isServiceRegisterd(MicroService m){
		return serviceToQueue.containsKey(m)&&serviceToQueue.get(m)!=null&&serviceToQueue.get(m) instanceof LinkedBlockingDeque;
	}
	public Boolean isServiceSubbedBC(MicroService m, Broadcast b){
		ConcurrentLinkedQueue<MicroService> subscribers = messageToService.get(b.getClass());
    	return subscribers != null && subscribers.contains(m);
	}
}
