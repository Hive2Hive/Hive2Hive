package org.hive2hive.processes.implementations.context;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;

public class DownloadFileContext implements IConsumeKeyPair, IProvideMetaDocument, IConsumeMetaDocument {

	private final PublicKey fileKey;
	private final String destinationFileName; // set null for default
	private final int versionToDownload; // set -1 for default

	private FileTreeNode fileNode;
	private MetaDocument metaDocument;

	public DownloadFileContext(PublicKey fileKey) {
		this(fileKey, null, -1);
	}

	public DownloadFileContext(PublicKey fileKey, String destinationFileName, int versionToDownload) {
		this.fileKey = fileKey;
		this.destinationFileName = destinationFileName;
		this.versionToDownload = versionToDownload;
	}

	public PublicKey getFileKey() {
		return fileKey;
	}

	public boolean isFolder() {
		return fileNode.isFolder();
	}

	public void setFileNode(FileTreeNode fileNode) {
		this.fileNode = fileNode;
	}

	public FileTreeNode getFileNode() {
		return fileNode;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public KeyPair consumeKeyPair() {
		return fileNode.getKeyPair();
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	public String getDestinationFileName() {
		return destinationFileName;
	}

	public int getVersionToDownload() {
		return versionToDownload;
	}
}
