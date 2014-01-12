package org.hive2hive.client.files.events;

import java.nio.file.Path;

public class FileAddedEvent extends FileEvent {

	public FileAddedEvent(Path filePath) {
		super(filePath);
	}
}
