package org.hive2hive.client.files.events;

import java.nio.file.Path;

public class FileModifiedEvent extends FileEvent {

	public FileModifiedEvent(Path filePath) {
		super(filePath);
	}
}
