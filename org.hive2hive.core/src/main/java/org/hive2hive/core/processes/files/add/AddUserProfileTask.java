package org.hive2hive.core.processes.files.add;

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Random;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.file.FileUtil;
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
public class AddUserProfileTask extends UserProfileTask implements IFileEventGenerator {

	private static final long serialVersionUID = -4568985873058024202L;

	private static final Logger logger = LoggerFactory.getLogger(AddUserProfileTask.class);

	private final Index addedFileIndex;
	private final PublicKey parentKey;

	private final int forkLimit = 2;

	public AddUserProfileTask(String sender, Index index, PublicKey parentKey) {
		super(sender);
		this.addedFileIndex = index;
		this.parentKey = parentKey;
	}

	@Override
	public void start() {
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.", e);
			return;
		}

		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
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

			logger.debug("Newly shared file '{}' received.", addedFileIndex.getName());
			// file is new, link parent and new child
			parentNode.addChild(addedFileIndex);
			addedFileIndex.setParent(parentNode);

			try {
				// upload the changes
				profileManager.readyToPut(userProfile, getId());
				logger.debug("Successfully updated the index '{}' in the own user profile.", addedFileIndex.getName());
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
				notifyOtherClients(new AddNotificationMessageFactory(addedFileIndex, parentKey));
				logger.debug("Notified other clients that a file has been updated by another user.");
			} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException
					| NoSessionException e) {
				logger.error("Could not notify other clients of me about the new file.", e);
			}

			// trigger event
			Path addedFilePath = FileUtil.getPath(session.getRoot(), addedFileIndex);
			networkManager.getEventBus().publish(new FileAddEvent(addedFilePath, addedFileIndex.isFile()));

			break;
		}
	}

}
