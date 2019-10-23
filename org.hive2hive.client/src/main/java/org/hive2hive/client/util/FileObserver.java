package org.hive2hive.client.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * Default implementation of a file observer. Internally uses the Apache Commons IO
 * {@link FileAlterationObserver} and {@link FileAlterationMonitor}.
 * 
 * @author Christian
 * 
 */
public class FileObserver {

	private final FileAlterationObserver observer;
	private final FileAlterationMonitor monitor;

	private boolean isRunning;

	/**
	 * A file observer that uses the specified interval to check for file changes.
	 * 
	 * @param rootDirectory the root directory to monitor
	 * @param interval the interval in milliseconds to observe changes
	 */
	public FileObserver(File rootDirectory, long interval) {
		this.observer = new FileAlterationObserver(rootDirectory);
		this.monitor = new FileAlterationMonitor(interval, observer);
	}

	/**
	 * A file observer that uses the default interval to check for file changes.
	 * 
	 * @param rootDirectory the root directory to monitor
	 */
	public FileObserver(File rootDirectory) {
		this(rootDirectory, 1000);
	}

	public void start() throws Exception {
		if (!isRunning) {
			monitor.start();
			isRunning = true;
		}
	}

	public void stop() throws Exception {
		stop(0);
	}

	public void stop(long ms) throws Exception {
		if (isRunning) {
			monitor.stop(ms);
			isRunning = false;
		}
	}

	public void addFileObserverListener(FileObserverListener listener) {
		observer.addListener(listener);
	}

	public void removeFileObserverListener(FileObserverListener listener) {
		observer.removeListener(listener);
	}

	public List<FileObserverListener> getFileObserverListeners() {
		List<FileObserverListener> listeners = new ArrayList<FileObserverListener>();

		// TODO check if this interface casting is allowed
		for (FileAlterationListener listener : observer.getListeners()) {
			listeners.add((FileObserverListener) listener);
		}
		return listeners;
	}

	public long getInterval() {
		return monitor.getInterval();
	}

	public boolean isRunning() {
		return isRunning;
	}

}
