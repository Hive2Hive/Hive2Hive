package org.hive2hive.client.files;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.client.files.events.FileAddedEvent;
import org.hive2hive.client.files.events.FileModifiedEvent;
import org.hive2hive.client.files.events.FileRemovedEvent;
import org.hive2hive.client.files.interfaces.IFileAddWatcher;
import org.hive2hive.client.files.interfaces.IFileModifyWatcher;
import org.hive2hive.client.files.interfaces.IFileObserver;
import org.hive2hive.client.files.interfaces.IFileRemoveWatcher;

/**
 * Abstract base class for classes that watch all kinds of file events (add,
 * remove, modify).
 * 
 * @author Christian
 * 
 */
public abstract class FileWatcher implements IFileAddWatcher,
		IFileRemoveWatcher, IFileModifyWatcher {

	private final List<IFileObserver> fileObserver = new ArrayList<IFileObserver>();

	@Override
	public void attachFileObserver(IFileObserver observer) {
		fileObserver.add(observer);
	}

	@Override
	public void detachFileObserver(IFileObserver observer) {
		fileObserver.remove(observer);
	}

	@Override
	public void notifyFileAdded(FileAddedEvent e) {
		for (IFileObserver observer : fileObserver) {
			observer.onFileAdded(e);
		}
	}

	@Override
	public void notifyFileRemoved(FileRemovedEvent e) {
		for (IFileObserver observer : fileObserver) {
			observer.onFileRemoved(e);
		}
	}

	@Override
	public void notifyFileModified(FileModifiedEvent e) {
		for (IFileObserver observer : fileObserver) {
			observer.onFileModified(e);
		}
	}
}
