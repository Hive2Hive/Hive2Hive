package org.hive2hive.client.files.interfaces;

import org.hive2hive.client.files.events.FileAddedEvent;
import org.hive2hive.client.files.events.FileModifiedEvent;
import org.hive2hive.client.files.events.FileRemovedEvent;

public interface IFileObserver {

	public void onFileAdded(FileAddedEvent e);
	
	public void onFileRemoved(FileRemovedEvent e);
	
	public void onFileModified(FileModifiedEvent e);	
}
