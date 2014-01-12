package org.hive2hive.client.files.events;

import java.nio.file.Path;

public abstract class FileEvent {
	
	private final Path filePath;
	
	public FileEvent(Path filePath){
		this.filePath = filePath;
	}

}
