package org.hive2hive.client.files.interfaces;


public interface IFileAdditionWatcher extends IFileWatcher {
	
	public void notifyFileAdded(FileAddedEvent e);

}
