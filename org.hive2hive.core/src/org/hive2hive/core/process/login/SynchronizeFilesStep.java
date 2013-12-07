package org.hive2hive.core.process.login;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
import org.hive2hive.core.process.util.FileRecursionUtil.FileProcessAction;

/**
 * Synchronizes the local files with the entries in the user profile:
 * <ul>
 * <li>Files that have been added to the user profile while the client was offline --> missing on disk</li>
 * <li>Files that have been added to the folder on disk while the client was offline --> missing in
 * userprofile</li>
 * <li>Files that have been changed during the client was offline. The changes could have been made in the
 * userprofile or on the local disc. If changes on both locations have been made, the version in the user
 * profile wins.</li>
 * <li>If a file was deleted on disk during offline phase, the file gets deleted in the DHT too.</li>
 * <li>If a file was deleted in the user profile, the file gets deleted on disk too.</li>
 * </ul>
 * 
 * @author Nico
 * 
 */
public class SynchronizeFilesStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SynchronizeFilesStep.class);

	// collects the problems of the concurrent processes executed in this step
	List<String> problems = new CopyOnWriteArrayList<String>();

	@Override
	public void start() {
		PostLoginProcessContext context = (PostLoginProcessContext) getProcess().getContext();
		FileManager fileManager = context.getFileManager();
		UserProfileManager profileManager = context.getProfileManager();

		FileSynchronizer synchronizer = null;
		try {
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			synchronizer = new FileSynchronizer(fileManager, userProfile);
		} catch (GetFailedException e) {
			getProcess().stop(e.getMessage());
			return;
		}

		/*
		 * Download the remotely added and updated files
		 */
		List<FileTreeNode> toDownload = synchronizer.getAddedRemotely();
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessTreeNode downloadProcess = startDownload(toDownload);

		/*
		 * Upload the locally added and updated files
		 */
		List<File> toUploadNewFiles = synchronizer.getAddedLocally();
		ProcessTreeNode uploadProcessNewFiles = startUpload(toUploadNewFiles, FileProcessAction.NEW_FILE);
		List<File> toUploadNewVersions = synchronizer.getUpdatedLocally();
		ProcessTreeNode uploadProcessNewVersions = startUpload(toUploadNewVersions,
				FileProcessAction.MODIFY_FILE);

		/*
		 * Delete the files in the DHT
		 */
		List<FileTreeNode> toDeleteInDHT = synchronizer.getDeletedLocally();
		ProcessTreeNode deleteProcess = startDelete(toDeleteInDHT);

		/*
		 * Delete the remotely deleted files
		 */
		List<File> toDeleteOnDisk = synchronizer.getDeletedRemotely();
		for (File file : toDeleteOnDisk) {
			file.delete();
		}

		while (!(downloadProcess.isDone() && uploadProcessNewFiles.isDone()
				&& uploadProcessNewVersions.isDone() && deleteProcess.isDone())) {
			try {
				logger.debug("Waiting until uploads and downloads finish...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		logger.debug("All uploads / downloads are done");
		for (String problem : problems) {
			logger.error("Problem occurred: " + problem);
		}

		if (context.getIsDefinedAsMaster()) {
			// TODO set when step is implemented
			// String userId = profileManager.getUserCredentials().getUserId();
			// HandleUserMessageQueueStep handleUmQueueStep = new HandleUserMessageQueueStep(userId);
			// GetUserMessageQueueStep getUMQueueStep = new GetUserMessageQueueStep(userId,
			// handleUmQueueStep);
			// context.setUserMessageQueueStep(getUMQueueStep);
			// getProcess().setNextStep(getUMQueueStep);
			getProcess().setNextStep(null);
		} else {
			// done with the post login process
			getProcess().setNextStep(null);
		}
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

	private ProcessTreeNode startUpload(List<File> toUpload, FileProcessAction action) {
		// synchronize the files that need to be uploaded into the DHT
		ProcessTreeNode rootProcess = FileRecursionUtil.buildProcessTreeUpload(toUpload, getNetworkManager(),
				action);

		if (toUpload.size() > 0)
			logger.debug("Start uploading new files with action " + action.name());

		rootProcess.start();
		return rootProcess;
	}

	private ProcessTreeNode startDelete(List<FileTreeNode> toDelete) {
		ProcessTreeNode rootProcess = FileRecursionUtil.buildProcessTreeForDeletion(toDelete,
				getNetworkManager());

		if (toDelete.size() > 0)
			logger.debug("Start deleting files in DHT...");

		rootProcess.start();
		return rootProcess;
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}
}
