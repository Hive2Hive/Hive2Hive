package org.hive2hive.client.files;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.client.files.interfaces.FileAddedEvent;
import org.hive2hive.client.files.interfaces.IFileAdditionWatcher;
import org.hive2hive.client.files.interfaces.IFileModificationWatcher;
import org.hive2hive.client.files.interfaces.IFileObserver;
import org.hive2hive.client.files.interfaces.IFileRemovalWatcher;

public class H2HFileWatcher implements IFileAdditionWatcher, IFileRemovalWatcher, IFileModificationWatcher {

	private List<IFileObserver> fileObserver = new ArrayList<IFileObserver>();
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyFileRemoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyFileModified() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
