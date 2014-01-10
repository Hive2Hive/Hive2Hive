package org.hive2hive.client.files.interfaces;

public interface IFileWatcher {
	
	public void attachFileObserver(IFileObserver observer);
	
	public void detachFileObserver(IFileObserver observer);
	
	public void start();
	
	public void stop();

}
