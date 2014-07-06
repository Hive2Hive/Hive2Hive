package org.hive2hive.core.processes.files.synchronize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.files.util.FileRecursionUtil;
import org.hive2hive.core.processes.files.util.FileRecursionUtil.FileProcessAction;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes the local files with the entries in the user profile:
 * <ul>
 * <li>Files that have been added to the user profile while the client was offline --> missing on disk</li>
 * <li>Files that have been added to the folder on disk while the client was offline --> missing in user
 * profile</li>
 * <li>Files that have been changed during the client was offline. The changes could have been made in the
 * user profile or on the local disc. If changes on both locations have been made, the version in the user
 * profile wins.</li>
 * <li>If a file was deleted on disk during offline phase, the file gets deleted in the DHT too.</li>
 * <li>If a file was deleted in the user profile, the file gets deleted on disk too.</li>
 * </ul>
 * 
 * @author Nico
 * 
 */
public class SynchronizeFilesStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(SynchronizeFilesStep.class);
	private final NetworkManager networkManager;

	public SynchronizeFilesStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfileManager profileManager;
		try {
			profileManager = networkManager.getSession().getProfileManager();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("User profile could not be loaded.");
		}

		FileSynchronizer synchronizer;
		try {
			synchronizer = new FileSynchronizer(networkManager.getSession().getRoot(), profile);
		} catch (NoSessionException | IOException e) {
			throw new ProcessExecutionException("FileSynchronizer could not be instantiated.", e);
		}
		try {
			logger.debug("Start synchronizing files");
			synchronizeFiles(synchronizer);
		} catch (NoSessionException | NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}
	}

	private void synchronizeFiles(FileSynchronizer synchronizer) throws NoSessionException, InvalidProcessStateException,
			NoPeerConnectionException {
		/*
		 * - add the uploadProcessNewFiles
		 * - add the uploadProcessNewVersions
		 * - add the deleteProess
		 * - add the downloadProcess
		 */

		// download remotely added/updated files
		List<Index> toDownload = new ArrayList<Index>(synchronizer.getAddedRemotely());
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(toDownload, networkManager);
		getParent().add(downloadProcess);

		// upload the locally added files
		List<Path> toUploadNewFiles = synchronizer.getAddedLocally();
		ProcessComponent addProcess = FileRecursionUtil.buildUploadProcess(toUploadNewFiles, FileProcessAction.NEW_FILE,
				networkManager);
		getParent().add(addProcess);

		// upload the locally updated files
		List<Path> toUploadModifiedFiles = synchronizer.getUpdatedLocally();
		ProcessComponent updateProcess = FileRecursionUtil.buildUploadProcess(toUploadModifiedFiles,
				FileProcessAction.MODIFY_FILE, networkManager);
		getParent().add(updateProcess);

		// remove files from the DHT that have been deleted locally
		List<Index> toDeleteInDHT = synchronizer.getDeletedLocally();
		ProcessComponent deletionProcess = FileRecursionUtil.buildDeletionProcessFromNodelist(toDeleteInDHT, networkManager);
		getParent().add(deletionProcess);

		// delete the remotely deleted files (is done directly here)
		List<Path> toDeleteOnDisk = synchronizer.getDeletedRemotely();
		for (Path path : toDeleteOnDisk) {
			path.toFile().delete();
		}
	}
}
