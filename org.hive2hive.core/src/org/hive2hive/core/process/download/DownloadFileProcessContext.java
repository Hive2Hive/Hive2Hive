package org.hive2hive.core.process.download;

import java.nio.file.Path;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class DownloadFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final FileTreeNode file;
	private final FileManager fileManager;
	private final Path destination;
	private final int indexToDownload;
	private MetaDocument metaDocument;

	public DownloadFileProcessContext(DownloadFileProcess process, FileTreeNode file,
			FileManager fileManager, Path destination, int indexToDownload) {
		super(process);
		this.file = file;
		this.fileManager = fileManager;
		this.destination = destination;
		this.indexToDownload = indexToDownload;
	}

	public FileTreeNode getFile() {
		return file;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	public Path getDestination() {
		return destination;
	}

	public int getIndexToDownload() {
		return indexToDownload;
	}
}
