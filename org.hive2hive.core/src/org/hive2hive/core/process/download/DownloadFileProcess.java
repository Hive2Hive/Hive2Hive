package org.hive2hive.core.process.download;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * Download a file that is already in the user profile but not on disk yet
 */
public class DownloadFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(DownloadFileProcess.class);

	private DownloadFileProcessContext context;

	/**
	 * Preferably use this constructor if the {@link FileTreeNode} is already existent.
	 * 
	 * @param file
	 * @param networkManager
	 * @param fileManager
	 */
	public DownloadFileProcess(FileTreeNode file, NetworkManager networkManager, FileManager fileManager) {
		super(networkManager);
		initialize(file, fileManager);
	}

	/**
	 * Use this constructor if the {@link FileTreeNode} is not here. The user profile will first be fetched
	 * and then the normal steps are executed. Note that a valid session must already be in place.
	 * 
	 * @param fileKey the public key of the file
	 * @param networkManager the network manager
	 * @throws GetFailedException
	 * @throws NoSessionException
	 */
	public DownloadFileProcess(PublicKey fileKey, NetworkManager networkManager) throws GetFailedException,
			NoSessionException {
		super(networkManager);

		UserProfileManager profileManager = networkManager.getSession().getProfileManager();
		UserProfile userProfile = profileManager.getUserProfile(super.getID(), false);
		FileTreeNode fileNode = userProfile.getFileById(fileKey);

		initialize(fileNode, networkManager.getSession().getFileManager());
	}

	private void initialize(FileTreeNode file, FileManager fileManager) {
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
