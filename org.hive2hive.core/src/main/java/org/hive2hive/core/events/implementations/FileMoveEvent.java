package org.hive2hive.core.events.implementations;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;

public class FileMoveEvent extends FileEvent implements IFileMoveEvent {

	private File dstFile;

	public FileMoveEvent(File srcFile, File dstFile,  boolean isFile) {
		super(srcFile, isFile);
		this.dstFile = dstFile;
	}

	@Override
	public File getSrcFile() {
		return getFile();
	}

	@Override
	public File getDstFile() {
		return dstFile;
	}

}
