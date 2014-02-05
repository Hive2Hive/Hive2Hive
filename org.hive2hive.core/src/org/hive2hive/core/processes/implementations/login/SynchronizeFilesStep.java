package org.hive2hive.core.processes.implementations.login;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;
import org.hive2hive.core.processes.implementations.context.LoginProcessContext;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil.FileProcessAction;

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

	private final LoginProcessContext context;
	private final NetworkManager networkManager;

	public SynchronizeFilesStep(LoginProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfileManager profileManager;
		FileManager fileManager;
		try {
			profileManager = networkManager.getSession().getProfileManager();
			fileManager = networkManager.getSession().getFileManager();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("User profile could not be loaded.");
		}

		FileSynchronizer synchronizer = new FileSynchronizer(fileManager, profile);
		try {
			synchronizeFiles(synchronizer);
		} catch (NoSessionException e) {
			throw new ProcessExecutionException(e);
		} catch (NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}

		if (context.getIsMaster()) {
			// If is master, process the user profile queue
			logger.debug("Starting to process all user tasks because I'm defined as master");
			getParent().add(ProcessFactory.instance().createUserProfileTaskStep(networkManager));
		}
	}

	private void synchronizeFiles(FileSynchronizer synchronizer) throws NoSessionException,
			InvalidProcessStateException, NoPeerConnectionException {
		/*
		 * count up to 4:
		 * - until the uploadProcessNewFiles is done
		 * - until the uploadProcessNewVersions is done
		 * - until the deleteProess is done
		 * - until the downloadProcess is done
		 */
		CountDownLatch latch = new CountDownLatch(4);
		CountDownListener latchListener = new CountDownListener(latch);

		// download remotely added/updated files
		List<FileTreeNode> toDownload = new ArrayList<FileTreeNode>(synchronizer.getAddedRemotely());
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(toDownload, networkManager);
		downloadProcess.attachListener(latchListener);
		downloadProcess.start();

		// upload the locally added files
		List<Path> toUploadNewFiles = synchronizer.getAddedLocally();
		ProcessComponent addProcess = FileRecursionUtil.buildUploadProcess(toUploadNewFiles,
				FileProcessAction.NEW_FILE, networkManager);
		addProcess.attachListener(latchListener);
		addProcess.start();

		// upload the locally updated files
		List<Path> toUploadModifiedFiles = synchronizer.getUpdatedLocally();
		ProcessComponent updateProcess = FileRecursionUtil.buildUploadProcess(toUploadModifiedFiles,
				FileProcessAction.MODIFY_FILE, networkManager);
		updateProcess.attachListener(latchListener);
		updateProcess.start();

		// remove files from the DHT that have been deleted locally
		List<FileTreeNode> toDeleteInDHT = synchronizer.getDeletedLocally();
		ProcessComponent deletionProcess = FileRecursionUtil.buildDeletionProcessFromNodelist(toDeleteInDHT,
				networkManager);
		deletionProcess.attachListener(latchListener);
		deletionProcess.start();

		// delete the remotely deleted files (is done directly here)
		List<Path> toDeleteOnDisk = synchronizer.getDeletedRemotely();
		for (Path path : toDeleteOnDisk) {
			path.toFile().delete();
		}

		// TODO check process state and if it does not change for a while, don't wait anymore (else, it may
		// cause an endless loop)
		while (latch.getCount() > 0) {
			try {
				logger.debug("Waiting until uploads and downloads finish...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		logger.debug("All uploads / downloads are done");
	}

	private class CountDownListener implements IProcessComponentListener {

		private final CountDownLatch latch;

		public CountDownListener(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onSucceeded() {
			latch.countDown();
		}

		@Override
		public void onFailed(RollbackReason reason) {
			latch.countDown();
		}

		@Override
		public void onFinished() {
			// ignore
		}

	}

}
