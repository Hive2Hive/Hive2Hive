package org.hive2hive.core.process.download;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

public class DownloadFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(DownloadFileProcess.class);

	private DownloadFileProcessContext context;

	/**
	 * Download a file that is already in the user profile but not on disk yet
	 * 
	 * @param file
	 * @param networkManager
	 * @param fileManager
	 */
	public DownloadFileProcess(FileTreeNode file, NetworkManager networkManager, FileManager fileManager) {
		super(networkManager);
		context = new DownloadFileProcessContext(this, file, fileManager);

		if (file.isFolder()) {
			logger.info("No download of the file needed since it's a folder");
			setNextStep(new CreateFolderStep(file, fileManager));
		} else {
			// download the file
			logger.info("Start downloading file " + file.getFullPath());

			// 1. get the meta file
			// 2. evaluate the meta file
			// 3. download all chunks
			GetMetaDocumentStep metaDocumentStep = new GetMetaDocumentStep(file.getKeyPair(),
					new EvaluateMetaDocumentStep(), context);
			setNextStep(metaDocumentStep);

		}
	}

	@Override
	public DownloadFileProcessContext getContext() {
		return context;
	}

}
