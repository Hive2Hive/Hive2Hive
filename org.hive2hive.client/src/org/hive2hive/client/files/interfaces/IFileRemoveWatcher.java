package org.hive2hive.client.files.interfaces;

import org.hive2hive.client.files.events.FileRemovedEvent;

/**
 * Interface for all classes that watch for {@link FileRemovedEvent}s.
 * 
 * @author Christian
 * 
 */
public interface IFileRemoveWatcher extends IFileWatcher {

	public void notifyFileRemoved(FileRemovedEvent e);
}
