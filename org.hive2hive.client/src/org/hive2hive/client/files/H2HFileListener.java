package org.hive2hive.client.files;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public class H2HFileListener implements FileAlterationListener {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HFileListener.class);

	@Override
	public void onStart(FileAlterationObserver observer) {
		logger.debug("onStart()");
	}

	@Override
	public void onDirectoryCreate(File directory) {
		logger.debug("onDirectoryCreate()");	
		printFileDetails("created", directory);
	}

	@Override
	public void onDirectoryChange(File directory) {
		logger.debug("onDirectoryChange()");	
		printFileDetails("changed", directory);
	}

	@Override
	public void onDirectoryDelete(File directory) {
		logger.debug("onDirectoryDelete()");	
		printFileDetails("deleted", directory);
	}

	@Override
	public void onFileCreate(File file) {
		logger.debug("onFileCreate()");		
		printFileDetails("created", file);
	}

	@Override
	public void onFileChange(File file) {
		logger.debug("onFileChange()");		
		printFileDetails("changed", file);
	}

	@Override
	public void onFileDelete(File file) {
		logger.debug("onFileDelete()");		
		printFileDetails("deleted", file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		logger.debug("onStop()");		
	}

	private void printFileDetails(String reason, File file) {
		System.out.printf("%s %s: %s\n", file.isDirectory() ? "Directory" : "File", reason, file.getAbsolutePath());
	}
}
