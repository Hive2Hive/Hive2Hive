package org.hive2hive.client.files.interfaces;

import org.hive2hive.client.files.events.FileModifiedEvent;

/**
 * Interface for all classes that watch for {@link FileModifiedEvent}s.
 * 
 * @author Christian
 * 
 */
public interface IFileModifyWatcher extends IFileWatcher {

	public void notifyFileModified(FileModifiedEvent e);
}
