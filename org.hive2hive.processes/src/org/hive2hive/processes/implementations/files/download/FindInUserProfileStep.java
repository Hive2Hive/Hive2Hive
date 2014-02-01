package org.hive2hive.processes.implementations.files.download;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.GetMetaDocumentStep;
import org.hive2hive.processes.implementations.context.DownloadFileContext;

public class FindInUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(FindInUserProfileStep.class);

	private final DownloadFileContext context;
	private final NetworkManager networkManager;

	public FindInUserProfileStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		UserProfile userProfile = null;
		try {
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException e) {
			cancel(new RollbackReason(this, e.getMessage()));
			return;
		}

		FileTreeNode fileNode = userProfile.getFileById(context.getFileKey());
		if (fileNode == null) {
			cancel(new RollbackReason(this, "File key not found in user profile"));
			return;
		}

		context.setFileNode(fileNode);

		// add the next steps here
		if (fileNode.isFolder()) {
			logger.info("No download of the file needed since '" + fileNode.getFullPath() + "' is a folder");
			getParent().add(new CreateFolderStep(context, networkManager));
		} else {
			logger.info("Initalize the process for downloading file " + fileNode.getFullPath());
			try {
				FileManager fileManager = networkManager.getSession().getFileManager();
				IDataManager dataManager = networkManager.getDataManager();
				getParent().add(new GetMetaDocumentStep(context, context, dataManager));
				getParent().add(new DownloadChunksStep(context, dataManager, fileManager));
			} catch (Hive2HiveException e) {
				cancel(new RollbackReason(this, e.getMessage()));
			}
		}
	}
}
