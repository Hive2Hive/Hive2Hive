package org.hive2hive.core.api.interfaces;

/**
 * Interface for any file observer.
 * @author Christian
 *
 */
public interface IFileObserver {
	
	/**
	 * Start the file observer.
	 * @throws Exception 
	 */
	void start() throws Exception;
	
	/**
	 * Stop the file observer.
	 * @throws Exception 
	 */
	void stop() throws Exception;
	
	void addFileObserverListener(IFileObserverListener listener);
	
	void removeFileObserverListener(IFileObserverListener listener);

}
