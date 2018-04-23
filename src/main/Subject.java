package webapi;

/**
 * Interface, which is the subject part of the observer design pattern, which itself is used to dynamically update DataSources for orders
 * @author Patryk Wegrzyn
 *
 */
public interface Subject {
	
	/**
	 * Registers a new observer.
	 * @param o Observer, which is supposed to be registered
	 */
	public void register(Observer o);
	/**
	 * Unregisters an existing observer.
	 * @param o Observer, which is supposed to be unregistered
	 */
	public void unregister(Observer o);
	/**
	 * Notifies all the registered observers about a new event.
	 */
	public void notifyObserver();

}
