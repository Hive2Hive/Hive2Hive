package org.hive2hive.core.processes.files.delete;

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Random;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
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
public class DeleteUserProfileTask extends UserProfileTask implements IFileEventGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DeleteUserProfileTask.class);

	private static final long serialVersionUID = 4580106953301162049L;

	private final PublicKey fileKey;

	private final int forkLimit = 2;

	public DeleteUserProfileTask(String sender, PublicKey fileKey) {
		super(sender);
		this.fileKey = fileKey;
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

		Index fileToDelete;
		FolderIndex parent;
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

			fileToDelete = userProfile.getFileById(fileKey);
			if (fileToDelete == null) {
				logger.error("Got notified about a file we don't know.");
				return;
			}

			parent = fileToDelete.getParent();
			if (parent == null) {
				logger.error("Got task to delete the root, which is invalid.");
				return;
			}

			// check write permission
			if (!parent.canWrite(sender)) {
				logger.error("User without WRITE permissions tried to delete a file.");
				return;
			}

			parent.removeChild(fileToDelete);

			try {
				profileManager.readyToPut(userProfile, getId());
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
				logger.error("Couldn't put updated user profile.", e);
				return;
			}

			break;
		}

		try {
			// notify own other clients
			notifyOtherClients(new DeleteNotifyMessageFactory(fileToDelete.getFilePublicKey(), parent.getFilePublicKey(),
					fileToDelete.getName(), fileToDelete.isFile()));
			logger.debug("Notified other clients that a file has been deleted by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the deleted file.", e);
		}

		// trigger event
		Path deletedFilePath = FileUtil.getPath(session.getRoot(), fileToDelete);
		networkManager.getEventBus().publish(new FileDeleteEvent(deletedFilePath, fileToDelete.isFile()));
	}

}
