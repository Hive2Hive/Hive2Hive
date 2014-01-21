package org.hive2hive.core.process.userprofiletask.share;

import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.util.FileRecursionUtil;

/**
 * Downloads all files that the other user shared with us
 * 
 * @author Seppi, Nico
 */
public class SynchronizeSharedFolderStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SynchronizeSharedFolderStep.class);

	@Override
	public void start() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess()
				.getContext();
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
		Process downloadProcess = FileRecursionUtil.buildProcessChainForDownload(toDownload,
				getNetworkManager());
		downloadProcess.start();
		try {
			logger.debug("Waiting until uploads and downloads finish...");
			downloadProcess.join();
			logger.debug("All downloads of the shared folder are done");
		} catch (IllegalProcessStateException | InterruptedException e) {
			logger.error("Got interrupted while waiting until all files are downloaded");
		}

		// we're done
		getProcess().setNextStep(null);
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}
}
