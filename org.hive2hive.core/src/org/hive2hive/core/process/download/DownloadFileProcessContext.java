package org.hive2hive.core.process.download;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

public class DownloadFileProcessContext extends ProcessContext {

	private final GetMetaDocumentStep metaDocumentStep;
	private final FileTreeNode file;
	private final FileManager fileManager;

	public DownloadFileProcessContext(DownloadFileProcess process, FileTreeNode file,
			GetMetaDocumentStep metaDocumentStep, FileManager fileManager) {
		super(process);
		this.file = file;
		this.metaDocumentStep = metaDocumentStep;
		this.fileManager = fileManager;
	}

	public GetMetaDocumentStep getMetaDocumentStep() {
		return metaDocumentStep;
	}

	public FileTreeNode getFile() {
		return file;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}
