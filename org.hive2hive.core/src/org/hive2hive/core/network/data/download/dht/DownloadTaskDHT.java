package org.hive2hive.core.network.data.download.dht;

import java.io.File;
import java.security.PrivateKey;
import java.util.List;

import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTaskDHT extends BaseDownloadTask {

	private static final long serialVersionUID = -6933011357191806148L;
	private final static Logger logger = LoggerFactory.getLogger(DownloadTaskDHT.class);

	public DownloadTaskDHT(List<MetaChunk> metaChunks, File destination, PrivateKey decryptionKey) {
		super(metaChunks, destination, decryptionKey);
	}

	@Override
	public boolean isDirectDownload() {
		return false;
	}
}
