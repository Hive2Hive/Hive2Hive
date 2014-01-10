package org.hive2hive.client.files.interfaces;

import java.nio.file.Path;

public abstract class FileEvent {
	
	private final Path filePath;
	
	public FileEvent(Path filePath){
		this.filePath = filePath;
	}

}
