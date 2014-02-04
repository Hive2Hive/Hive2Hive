package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DownloadFileContext implements IConsumeKeyPair, IProvideMetaDocument, IConsumeMetaDocument {

	// set -1 for default
	public static final int NEWEST_VERSION_INDEX = -1;

	private final PublicKey fileKey;
	private final File destination; // set null for default
	private final int versionToDownload;

	private FileTreeNode fileNode;
	private MetaDocument metaDocument;

	public DownloadFileContext(PublicKey fileKey, File destination, int versionToDownload) {
		this.fileKey = fileKey;
		this.destination = destination;
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
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	@Override
	public KeyPair consumeKeyPair() {
		return fileNode.getKeyPair();
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	public File getDestination() {
		return destination;
	}

	public int getVersionToDownload() {
		return versionToDownload;
	}
}
