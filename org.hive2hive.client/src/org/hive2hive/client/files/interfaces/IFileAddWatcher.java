package org.hive2hive.client.files.interfaces;

import org.hive2hive.client.files.events.FileAddedEvent;

/**
 * Interface for all classes that watch for {@link FileAddedEvent}s.
 * 
 * @author Christian
 * 
 */
public interface IFileAddWatcher extends IFileWatcher {

	public void notifyFileAdded(FileAddedEvent e);

}
