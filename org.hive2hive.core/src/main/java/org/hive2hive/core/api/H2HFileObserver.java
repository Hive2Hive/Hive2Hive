package org.hive2hive.core.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;

/**
 * Default implementation of {@link IFileObserver}. Internally uses the Apache Commons IO
 * {@link FileAlterationObserver} and {@link FileAlterationMonitor}.
 * 
 * @author Christian
 * 
 */
public class H2HFileObserver implements IFileObserver {

	private final FileAlterationObserver observer;
	private final FileAlterationMonitor monitor;

	private boolean isRunning;

	/**
	 * A file observer that uses the specified interval to check for file changes.
	 * 
	 * @param rootDirectory
	 * @param ms
	 */
	public H2HFileObserver(File rootDirectory, long ms) {
		this.observer = new FileAlterationObserver(rootDirectory);
		this.monitor = new FileAlterationMonitor(ms, observer);
	}

	/**
	 * A file observer that uses the default interval to check for file changes.
	 * 
	 * @param rootDirectory
	 */
	public H2HFileObserver(File rootDirectory) {
		this(rootDirectory, H2HConstants.DEFAULT_FILE_OBSERVER_INTERVAL);
	}

	@Override
	public void start() throws Exception {
		if (!isRunning) {
			monitor.start();
			isRunning = true;
		}
	}

	@Override
	public void stop() throws Exception {
		stop(0);
	}

	public void stop(long ms) throws Exception {
		if (isRunning) {
			monitor.stop(ms);
			isRunning = false;
		}
	}

	@Override
	public void addFileObserverListener(IFileObserverListener listener) {
		observer.addListener(listener);
	}

	@Override
	public void removeFileObserverListener(IFileObserverListener listener) {
		observer.removeListener(listener);
	}

	@Override
	public List<IFileObserverListener> getFileObserverListeners() {
		List<IFileObserverListener> listeners = new ArrayList<IFileObserverListener>();

		// TODO check if this interface casting is allowed
		for (FileAlterationListener listener : observer.getListeners()) {
			listeners.add((IFileObserverListener) listener);
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
