package org.hive2hive.core.api.interfaces;

/**
 * Interface for any file observer.
 * @author Christian
 *
 */
public interface IFileObserver {
	
	/**
	 * Sets the file observation interval in milliseconds.
	 * @param ms The interval in milliseconds.
	 */
	void setInterval(long ms);
	
	/**
	 * Start the file observer.
	 */
	void start();
	
	/**
	 * Stop the file observer.
	 */
	void stop();

}
