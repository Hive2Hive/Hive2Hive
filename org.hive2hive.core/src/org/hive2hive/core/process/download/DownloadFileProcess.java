package org.hive2hive.core.process.download;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.security.H2HEncryptionUtil;

public class DownloadFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(DownloadFileProcess.class);

	private DownloadFileProcessContext context;

	/**
	 * Download a file that is already in the user profile but not on disk yet
	 */
	public DownloadFileProcess(FileTreeNode file, NetworkManager networkManager, FileManager fileManager) {
		super(networkManager);
		context = new DownloadFileProcessContext(this, file, fileManager);

		// check if already exists
		File existing = fileManager.getFile(file);
		if (existing != null && existing.exists()) {
			try {
				if (H2HEncryptionUtil.compareMD5(existing, file.getMD5())) {
					logger.info("File already exists on disk. Content does match; no download needed");
					return;
				} else {
					logger.warn("File already exists on disk, it will be overwritten");
				}
			} catch (IOException e) {
				logger.warn("File already exists on disk, it will be overwritten");
			}
		}

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
