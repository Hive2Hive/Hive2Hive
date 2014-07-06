package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DownloadFileContext implements IGetMetaFileContext {

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
	public KeyPair consumeMetaFileEncryptionKeys() {
		return index.getFileKeys();
	}

	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	public File getDestination() {
		return destination;
	}

	/**
	 * Returns whether the download should happen to the default destination, same as in user profile
	 */
	public boolean downloadToDefaultDestination() {
		return destination == null;
	}

	public int getVersionToDownload() {
		return versionToDownload;
	}

	/**
	 * Returns whether the newest version should be downloaded
	 */
	public boolean downloadNewestVersion() {
		return versionToDownload == NEWEST_VERSION_INDEX;
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	public Index consumeIndex() {
		return index;
	}
}
