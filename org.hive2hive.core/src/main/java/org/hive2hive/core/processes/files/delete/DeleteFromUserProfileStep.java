package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.core.processes.files.GetMetaFileStep;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step that deletes a file from the index in the user profile after doing some verification.
 * In case the deleted file is a file (and not a folder), this step initiates the deletion of the meta file
 * and all according chunks.
 * 
 * @author Nico, Seppi
 */
public class DeleteFromUserProfileStep extends BaseGetProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(DeleteFromUserProfileStep.class);

	private final DeleteFileProcessContext context;
	private final UserProfileManager profileManager;
	private final DataManager dataManager;

	private final int forkLimit = 2;

	public DeleteFromUserProfileStep(DeleteFileProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException, NoSessionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.dataManager = networkManager.getDataManager();
		this.profileManager = networkManager.getSession().getProfileManager();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		Path root = context.consumeRoot();

		Index fileIndex;
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			// get user profile
			UserProfile profile = null;
			try {
				profile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				throw new ProcessExecutionException("Could not get user profile.", e);
			}

			fileIndex = profile.getFileByPath(file, root);

			// validate
			if (fileIndex == null) {
				throw new ProcessExecutionException("File index not found in user profile");
			} else if (!fileIndex.canWrite()) {
				throw new ProcessExecutionException("Not allowed to delete this file (read-only permissions)");
			}

			// check preconditions
			if (fileIndex.isFolder()) {
				FolderIndex folder = (FolderIndex) fileIndex;
				if (!folder.getChildren().isEmpty()) {
					throw new ProcessExecutionException("Cannot delete a directory that is not empty.");
				}
			}

			// remove the node from the tree
			FolderIndex parentIndex = fileIndex.getParent();
			parentIndex.removeChild(fileIndex);

			// store for later
			context.provideIndex(fileIndex);

			try {
				profileManager.readyToPut(profile, getID());
			} catch (VersionForkAfterPutException e) {
				if (forkCounter++ > forkLimit) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;

					// retry update of user profile
					continue;
				}
			} catch (PutFailedException e) {
				logger.error("Cannot remove the file {} from the user profile", fileIndex.getFullPath(), e);
				throw new ProcessExecutionException("Could not put user profile.");
			}

			break;
		}

		if (fileIndex.isFile()) {
			context.provideProtectionKeys(fileIndex.getProtectionKeys());
			context.provideMetaFileEncryptionKeys(fileIndex.getFileKeys());

			// create steps to delete meta and all chunks
			GetMetaFileStep getMeta = new GetMetaFileStep(context, dataManager);
			DeleteChunksProcess deleteChunks = new DeleteChunksProcess(context, dataManager);
			DeleteMetaFileStep deleteMeta = new DeleteMetaFileStep(context, dataManager);

			// insert them in correct order
			getParent().insertNext(getMeta, this);
			getParent().insertNext(deleteChunks, getMeta);
			getParent().insertNext(deleteMeta, deleteChunks);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		File file = context.consumeFile();
		Path root = context.consumeRoot();
		Index index = context.consumeIndex();

		if (index != null) {
			int forkCounter = 0;
			int forkWaitTime = new Random().nextInt(1000) + 500;
			while (true) {
				// get user profile
				UserProfile profile = null;
				try {
					profile = profileManager.getUserProfile(getID(), true);
				} catch (GetFailedException e) {
					logger.warn("Rollback failed.", e);
					return;
				}

				// re-add file to user profile
				FolderIndex parent = (FolderIndex) profile.getFileByPath(file.getParentFile(), root);
				parent.addChild(index);
				index.setParent(parent);

				try {
					profileManager.readyToPut(profile, getID());
				} catch (VersionForkAfterPutException e) {
					if (forkCounter++ > forkLimit) {
						logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
					} else {
						logger.warn("Version fork after put detected. Rejecting and retrying put.");

						// exponential back off waiting
						try {
							Thread.sleep(forkWaitTime);
						} catch (InterruptedException e1) {
							// ignore
						}
						forkWaitTime = forkWaitTime * 2;

						// retry update of user profile
						continue;
					}
				} catch (PutFailedException e) {
					logger.warn("Rollback failed.", e);
					return;
				}

				break;
			}
		}
	}

}
