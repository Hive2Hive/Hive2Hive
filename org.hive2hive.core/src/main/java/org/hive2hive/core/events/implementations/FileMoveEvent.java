package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;

public class FileMoveEvent extends FileEvent implements IFileMoveEvent {

	private Path dstPath;

	public FileMoveEvent(Path srcPath, Path dstPath, boolean isFile) {
		super(srcPath, isFile);
		this.dstPath = dstPath;
	}

	@Override
	public Path getSrcPath() {
		return getPath();
	}

	@Override
	public Path getDstPath() {
		return dstPath;
	}

}
