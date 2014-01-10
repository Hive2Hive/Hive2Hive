package org.hive2hive.client.files.interfaces;

import java.nio.file.Path;

public class FileAddedEvent extends FileEvent {

	public FileAddedEvent(Path filePath) {
		super(filePath);
	}

	
}
