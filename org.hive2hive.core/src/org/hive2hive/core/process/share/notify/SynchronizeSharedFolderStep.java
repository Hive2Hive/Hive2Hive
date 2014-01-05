package org.hive2hive.core.process.share.notify;

import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.util.FileRecursionUtil;
import org.hive2hive.core.process.util.ProcessTreeNode;

/**
 * 
 * @author Seppi
 */
public class SynchronizeSharedFolderStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SynchronizeSharedFolderStep.class);

	@Override
	public void start() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess().getContext();
		UserProfileManager profileManager = context.getProfileManager();
		FileManager fileManager = context.getFileManager();

		FileSynchronizer synchronizer = null;
		try {
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			synchronizer = new FileSynchronizer(fileManager, userProfile);
		} catch (GetFailedException e) {
			getProcess().stop(e);
			return;
		}

		/*
		 * Download the remotely added and updated files
		 */
		List<FileTreeNode> toDownload = synchronizer.getAddedRemotely();
		ProcessTreeNode downloadProcess = startDownload(toDownload);

		// TODO check process state and if it does not change for a while, don't wait anymore (else, it may
		// cause an endless loop)
		while (!downloadProcess.isDone()) {
			try {
				logger.debug("Waiting until uploads and downloads finish...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		logger.debug("All downloads of the shared folder are done");

		getProcess().setNextStep(null);
	}

	private ProcessTreeNode startDownload(List<FileTreeNode> toDownload) {
		ProcessTreeNode rootProcess = FileRecursionUtil.buildProcessTreeForDownload(toDownload,
				getNetworkManager());

		// start the download
		if (toDownload.size() > 0)
			logger.debug("Start downloading new and modified files...");

		rootProcess.start();
		return rootProcess;
	}


	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}
}
