package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeIndex;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideIndex;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DownloadFileContext implements IConsumeKeyPair, IProvideMetaFile, IConsumeMetaFile,
		IConsumeIndex, IProvideIndex {

	// set -1 for default
	public static final int NEWEST_VERSION_INDEX = -1;

	private final PublicKey fileKey;
	private final File destination; // set null for default
	private final int versionToDownload;

	private Index index;
	private MetaFile metaFile;

	public DownloadFileContext(PublicKey fileKey, File destination, int versionToDownload) {
		this.fileKey = fileKey;
		this.destination = destination;
		this.versionToDownload = versionToDownload;
	}

	public PublicKey getFileKey() {
		return fileKey;
	}

	public boolean isFolder() {
		return index.isFolder();
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	@Override
	public KeyPair consumeKeyPair() {
		return index.getFileKeys();
	}

	@Override
	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	public File getDestination() {
		return destination;
	}

	public int getVersionToDownload() {
		return versionToDownload;
	}

	@Override
	public void provideIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}
}
