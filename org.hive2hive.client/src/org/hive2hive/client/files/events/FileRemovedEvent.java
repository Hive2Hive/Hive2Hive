package org.hive2hive.client.files.events;

import java.nio.file.Path;

public class FileRemovedEvent extends FileEvent {

	public FileRemovedEvent(Path filePath) {
		super(filePath);
	}
}
