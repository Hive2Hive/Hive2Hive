package org.hive2hive.core.process.login;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.util.FileRecursionUtil;
import org.hive2hive.core.process.util.FileRecursionUtil.FileProcessAction;
import org.hive2hive.core.process.util.ProcessTreeNode;

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
		LoginProcessContext context = (LoginProcessContext) getProcess().getContext();
		UserProfileManager profileManager = context.getSession().getProfileManager();
		FileManager fileManager = context.getSession().getFileManager();

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
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessTreeNode downloadProcess = startDownload(toDownload);

		/*
		 * Upload the locally added and updated files
		 * count up to 2:
		 * - until the uploadProcessNewFiles is done (using listeners)
		 * - until the uploadProcessNewVersions is done (using listeners)
		 */
		CountDownLatch latch = new CountDownLatch(2);
		List<Path> toUploadNewFiles = synchronizer.getAddedLocally();
		Process uploadProcessNewFiles = startUpload(toUploadNewFiles, FileProcessAction.NEW_FILE);
		uploadProcessNewFiles.addListener(new CountdownListener(latch));

		List<Path> toUploadNewVersions = synchronizer.getUpdatedLocally();
		Process uploadProcessNewVersions = startUpload(toUploadNewVersions, FileProcessAction.MODIFY_FILE);
		uploadProcessNewVersions.addListener(new CountdownListener(latch));

		/*
		 * Delete the files in the DHT
		 */
		List<FileTreeNode> toDeleteInDHT = synchronizer.getDeletedLocally();
		ProcessTreeNode deleteProcess = startDelete(toDeleteInDHT);

		/*
		 * Delete the remotely deleted files
		 */
		List<Path> toDeleteOnDisk = synchronizer.getDeletedRemotely();
		for (Path path : toDeleteOnDisk) {
			path.toFile().delete();
		}

		// TODO check process state and if it does not change for a while, don't wait anymore (else, it may
		// cause an endless loop)
		while (!(downloadProcess.isDone() && deleteProcess.isDone()) && latch.getCount() > 0) {
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

		// if (context.getIsDefinedAsMaster()) {
		// // TODO set when step is implemented
		// // String userId = profileManager.getUserCredentials().getUserId();
		// // HandleUserMessageQueueStep handleUmQueueStep = new HandleUserMessageQueueStep(userId);
		// // GetUserMessageQueueStep getUMQueueStep = new GetUserMessageQueueStep(userId,
		// // handleUmQueueStep);
		// // context.setUserMessageQueueStep(getUMQueueStep);
		// // getProcess().setNextStep(getUMQueueStep);
		// getProcess().setNextStep(null);
		// } else {
		// // done with the post login process
		// getProcess().setNextStep(null);
		// }
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

	private Process startUpload(List<Path> toUpload, FileProcessAction action) {
		// synchronize the files that need to be uploaded into the DHT
		Process rootProcess = FileRecursionUtil.buildProcessList(toUpload, getNetworkManager(), action);

		if (toUpload.size() > 0) {
			logger.debug("Start uploading new files with action " + action.name());
		}

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

	/**
	 * Simply counts down the latch
	 * 
	 * @author Nico
	 * 
	 */
	private class CountdownListener implements IProcessListener {

		private final CountDownLatch latch;

		public CountdownListener(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onSuccess() {
			latch.countDown();
		}

		@Override
		public void onFail(Exception exception) {
			latch.countDown();
		}
	}
}