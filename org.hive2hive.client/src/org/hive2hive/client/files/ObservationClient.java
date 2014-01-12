package org.hive2hive.client.files;

import java.io.File;
import java.nio.file.Paths;

public class ObservationClient {
	
	public static void main(String[] args) {
		
		File rootDirectory = new File(Paths.get(System.getProperty("user.home"), "Hive2Hive").toString());
		
		H2HFileWatcher watcher = new H2HFileWatcher.H2HFileWatcherBuilder(rootDirectory).setInterval(1000).build();
		H2HFileListener listener = new H2HFileListener();
		
		watcher.addFileListener(listener);
		try {
			watcher.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
