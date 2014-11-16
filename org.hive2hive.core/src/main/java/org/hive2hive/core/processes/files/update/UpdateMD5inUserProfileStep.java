package org.hive2hive.core.processes.files.update;

import java.io.IOException;
import java.util.Random;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.UpdateFileProcessContext;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A step updating the MD5 hash in the user profile
 * 
 * @author Nico, Seppi
 */
public class UpdateMD5inUserProfileStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(UpdateMD5inUserProfileStep.class);

	private final UpdateFileProcessContext context;
	private final UserProfileManager profileManager;

	private final int forkLimit = 2;

	private byte[] originalMD5;

	public UpdateMD5inUserProfileStep(UpdateFileProcessContext context, UserProfileManager profileManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected Void doExecute() throws ProcessExecutionException {
		
		BaseMetaFile metaFile = context.consumeMetaFile();
		byte[] newMD5;
		try {
			newMD5 = HashUtil.hash(context.consumeFile());
		} catch (IOException ex) {
			throw new ProcessExecutionException(this, ex, "The new MD5 hash for the user profile could not be generated.");
		}

		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			try {
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);
				FileIndex index = (FileIndex) userProfile.getFileById(metaFile.getId());

				// store hash of meta file
				index.setMetaFileHash(context.consumeHash());

				// store for backup
				originalMD5 = index.getMD5();
				if (HashUtil.compare(originalMD5, newMD5)) {
					throw new ProcessExecutionException(this, "Try to create a new version with same content.");
				}

				// make and put modifications
				index.setMD5(newMD5);
				logger.debug("Updating the MD5 hash in the user profile.");
				profileManager.readyToPut(userProfile, getID());

				// store for notification
				context.provideIndex(index);
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
			} catch (GetFailedException | PutFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}

			break;
		}
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {
		
		BaseMetaFile metaFile = context.consumeMetaFile();

		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException ex) {
				throw new ProcessRollbackException(this, ex, "Couldn't get user profile and redo modifications.");
			}

			FileIndex fileNode = (FileIndex) userProfile.getFileById(metaFile.getId());
			fileNode.setMD5(originalMD5);

			try {
				profileManager.readyToPut(userProfile, getID());
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
			} catch (PutFailedException ex) {
				throw new ProcessRollbackException(this, ex, "Couldn't redo put of user profile.");
			}

			break;
		}
		setRequiresRollback(false);
		return null;
	}
}
