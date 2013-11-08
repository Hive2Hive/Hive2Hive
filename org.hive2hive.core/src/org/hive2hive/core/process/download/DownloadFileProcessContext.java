package org.hive2hive.core.process.download;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.ProcessContext;

public class DownloadFileProcessContext extends ProcessContext {

	private final FileTreeNode file;
	private final FileManager fileManager;

	public DownloadFileProcessContext(DownloadFileProcess process, FileTreeNode file, FileManager fileManager) {
		super(process);
		this.file = file;
		this.fileManager = fileManager;
	}

	public FileTreeNode getFile() {
		return file;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}
