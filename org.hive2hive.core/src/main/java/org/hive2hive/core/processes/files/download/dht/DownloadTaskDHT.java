package org.hive2hive.core.processes.files.download.dht;

import java.io.File;
import java.security.PrivateKey;
import java.util.List;

import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.download.BaseDownloadTask;

public class DownloadTaskDHT extends BaseDownloadTask {

	private static final long serialVersionUID = -6933011357191806148L;

	private final PrivateKey decryptionKey;

	public DownloadTaskDHT(List<MetaChunk> metaChunks, File destination, PrivateKey decryptionKey) {
		super(metaChunks, destination);
		this.decryptionKey = decryptionKey;
	}

	public PrivateKey getDecryptionKey() {
		return decryptionKey;
	}

	@Override
	public boolean isDirectDownload() {
		return false;
	}
}
