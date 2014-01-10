package org.hive2hive.client.files.interfaces;


public interface IFileObserver {

	public void onFileAdded(FileAddedEvent e);
	
	public void onFileRemoved();
	
	public void onFileModified();	
}
