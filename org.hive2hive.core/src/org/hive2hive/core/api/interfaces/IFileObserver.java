package org.hive2hive.core.api.interfaces;

import java.util.List;

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
	
	/**
	 * Adds a {@link IFileObserverListener} to this observer.
	 * @param listener The {@link IFileObserverListener} to be added.
	 */
	void addFileObserverListener(IFileObserverListener listener);
	
	/**
	 * Removes a {@link IFileObserverListener} from this observer.
	 * @param listener The {@link IFileObserverListener} to be removed.
	 */
	void removeFileObserverListener(IFileObserverListener listener);
	
	/**
	 * Returns a list of all attached {@link IFileObserverListener}s.
	 * @return List of all attached {@link IFileObserverListener}s.
	 */
	List<IFileObserverListener> getFileObserverListeners();

	/**
	 * Returns whether the file observer is running.
	 * @return <code>true</code> if running, <code>false</code> otherwise
	 */
	boolean isRunning();

}
