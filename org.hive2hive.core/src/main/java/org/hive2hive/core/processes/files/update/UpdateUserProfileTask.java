package org.hive2hive.core.processes.files.update;

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Random;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileUpdateEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class UpdateUserProfileTask extends UserProfileTask implements IFileEventGenerator {

	private static final long serialVersionUID = -4568985873058024202L;

	private static final Logger logger = LoggerFactory.getLogger(UpdateUserProfileTask.class);

	private final Index updatedFileIndex;
	private final PublicKey parentKey;

	private final int forkLimit = 2;

	public UpdateUserProfileTask(String sender, Index updatedFileIndex, PublicKey parentKey) {
		super(sender);
		this.updatedFileIndex = updatedFileIndex;
		this.parentKey = parentKey;
	}

	@Override
	public void start() {
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			H2HSession session;
			try {
				session = networkManager.getSession();
			} catch (NoSessionException e) {
				logger.error("No user seems to be logged in.", e);
				return;
			}

			UserProfileManager profileManager = session.getProfileManager();

			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getId(), true);
			} catch (GetFailedException e) {
				logger.error("Couldn't load user profile.", e);
				return;
			}

			FolderIndex parentNode = (FolderIndex) userProfile.getFileById(parentKey);
			if (parentNode == null) {
				logger.error("Could not process the task because the parent node has not been found.");
				return;
			}

			// validate if the other sharer has the right to share
			if (parentNode.canWrite(sender)) {
				logger.debug("Rights of user '{}' checked. User is allowed to modify.", sender);
			} else {
				logger.error("Permission of user '{}' not found. Deny to apply this user's changes.", sender);
				return;
			}

			// copy the md5 parameter of the received file
			Index existing = parentNode.getChildByName(updatedFileIndex.getName());
			if (existing.isFile() && updatedFileIndex.isFile()) {
				logger.debug("File update in a shared folder received: '{}'.", updatedFileIndex.getName());
				FileIndex existingFile = (FileIndex) existing;
				FileIndex newFile = (FileIndex) updatedFileIndex;
				existingFile.setMD5(newFile.getMD5());
			}

			try {
				// upload the changes
				profileManager.readyToPut(userProfile, getId());
				logger.debug("Successfully updated the index '{}' in the own user profile.", updatedFileIndex.getName());
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
				logger.error("Couldn't put updated user profile.");
				return;
			}

			try {
				// notify own other clients
				notifyOtherClients(new UpdateNotificationMessageFactory(updatedFileIndex, parentKey));
				logger.debug("Notified other clients that a file has been updated by another user.");
			} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException
					| NoSessionException e) {
				logger.error("Could not notify other clients of me about the updated file.", e);
			}

			// trigger event
			Path updatedFilePath = FileUtil.getPath(session.getRoot(), updatedFileIndex);
			networkManager.getEventBus().publish(new FileUpdateEvent(updatedFilePath, updatedFileIndex.isFile()));

			break;
		}
	}

}
