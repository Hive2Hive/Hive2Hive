package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;

/**
 * Context to download a file from the network. There are two modes:<br>
 * <ul>
 * <li>Download by giving the file key</li>
 * <li>Download by giving the full path of the file</li>
 * </ul>
 * 
 * @author Nico
 *
 */
public class DownloadFileContext implements IGetMetaFileContext {

	// set -1 for default
	public static final int NEWEST_VERSION_INDEX = -1;

	private final PublicKey fileKey;
	private final File file;
	private final File destination; // set null for default
	private final int versionToDownload;

	private Index index;
	private BaseMetaFile metaFile;

	public DownloadFileContext(PublicKey fileKey, File file, File destination, int versionToDownload) {
		this.fileKey = fileKey;
		this.file = file;
		this.destination = destination;
		this.versionToDownload = versionToDownload;
	}

	public PublicKey getFileKey() {
		return fileKey;
	}

	public File getFile() {
		return file;
	}

	@Override
	public void provideMetaFile(BaseMetaFile metaFile) {
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

	public BaseMetaFile consumeMetaFile() {
		return metaFile;
	}

	public File getDestination() {
		return destination;
	}

	/**
	 * @return Returns whether the download should happen to the default destination, same as in user profile
	 */
	public boolean downloadToDefaultDestination() {
		return destination == null;
	}

	public int getVersionToDownload() {
		return versionToDownload;
	}

	/**
	 * @return Returns whether the newest version should be downloaded
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
