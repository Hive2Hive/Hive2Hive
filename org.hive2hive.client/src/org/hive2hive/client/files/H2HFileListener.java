package org.hive2hive.client.files;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.Connection;

public class H2HFileListener implements FileAlterationListener {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HFileListener.class);

	@Override
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileCreate(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileChange(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		
	}

}
