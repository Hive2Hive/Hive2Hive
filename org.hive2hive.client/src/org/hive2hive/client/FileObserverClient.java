package org.hive2hive.client;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.file.watcher.H2HFileWatcher;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public class FileObserverClient {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileObserverClient.class);
	
	public static void main(String args[]) {

		LoggerInit.initLogger();
		
		H2HFileWatcher watcher = new H2HFileWatcher.H2HFileWatcherBuilder(Paths.get(
				FileUtils.getUserDirectoryPath(), "Hive2Hive").toFile()).setInterval(1000).build();
		watcher.addFileListener(new FileAlterationListener(){
			public void onStart(FileAlterationObserver observer) {
			}
			public void onDirectoryCreate(File directory) {
				printFileDetails("created", directory);
			}
			public void onDirectoryChange(File directory) {
				printFileDetails("changed", directory);
			}
			public void onDirectoryDelete(File directory) {
				printFileDetails("deleted", directory);
			}
			public void onFileCreate(File file) {
				printFileDetails("created", file);
			}
			public void onFileChange(File file) {
				printFileDetails("changed", file);
			}
			public void onFileDelete(File file) {
				printFileDetails("deleted", file);
			}
			public void onStop(FileAlterationObserver observer) {
			}
		});
		try {
			watcher.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printFileDetails(String reason, File file) {
		logger.debug(String.format("%s %s: %s\n", file.isDirectory() ? "Directory" : "File", reason,
				file.getAbsolutePath()));
	}
}
