package org.hive2hive.client.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.hive2hive.client.LoggerInit;

public class ObservationClient {
	
	public static void main(String[] args) {
		
		LoggerInit.initLogger();
		
		Path rootPath = Paths.get(System.getProperty("user.home"), "Hive2Hive");
		File rootDirectory = new File(rootPath.toString());
		try {
			if (!Files.exists(rootPath, LinkOption.NOFOLLOW_LINKS)){
				FileUtils.forceMkdir(rootDirectory);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		H2HFileWatcher watcher = new H2HFileWatcher.H2HFileWatcherBuilder(rootDirectory).setInterval(3000).build();
		H2HFileListener listener = new H2HFileListener();
		
		watcher.addFileListener(listener);
		try {
			watcher.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
