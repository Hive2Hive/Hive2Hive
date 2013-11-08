package org.hive2hive.core.process.download;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class DownloadFileProcess extends Process {

	private final DownloadFileProcessContext context;

	public DownloadFileProcess(FileTreeNode file, NetworkManager networkManager, FileManager fileManager) {
		super(networkManager);
		context = new DownloadFileProcessContext(this, file, fileManager);

		// TODO download the file
	}

	@Override
	public DownloadFileProcessContext getContext() {
		return context;
	}

}
