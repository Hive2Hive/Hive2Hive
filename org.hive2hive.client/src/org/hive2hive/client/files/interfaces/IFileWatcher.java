package org.hive2hive.client.files.interfaces;

import org.apache.commons.io.monitor.FileAlterationListener;

public interface IFileWatcher {
	
	public void attachFileListener(FileAlterationListener listener);
	
	public void detachFileListener(FileAlterationListener listener);
	
	public void start();
	
	public void stop();

}
