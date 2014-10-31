package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.UUID;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserProfileTask} that is pushed into the queue when a shared file is deleted. It removes the dead
 * reference in the user profile of this user and also removes the file on disk.
 * 
 * @author Nico
 * 
 */
public class DeleteUserProfileTask extends UserProfileTask implements IFileEventGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DeleteUserProfileTask.class);

	private static final long serialVersionUID = 4580106953301162049L;
	private final PublicKey fileKey;

	public DeleteUserProfileTask(String sender, PublicKey fileKey) {
		super(sender);
		this.fileKey = fileKey;
	}

	@Override
	public void start() {
		try {
			H2HSession session = networkManager.getSession();

			// remove dead link from user profile
			Index toDelete = updateUserProfile(session.getProfileManager());

			if (toDelete == null) {
				return;
			}

			// remove the file on disk
			removeFileOnDisk(session.getRootFile(), toDelete);

			// notify others
			startNotification(toDelete);
		} catch (Hive2HiveException | InvalidProcessStateException e) {
			logger.error("Could not execute the task.", e);
		}
	}

	/**
	 * Removes the {@link FolderIndex} in the user profile
	 * 
	 * @param profileManager
	 * @return the removed node
	 */
	private Index updateUserProfile(UserProfileManager profileManager) throws GetFailedException, PutFailedException {
		String randomPID = UUID.randomUUID().toString();

		Index toDelete = null;
		while (true) {
			UserProfile userProfile = profileManager.getUserProfile(randomPID, true);
			toDelete = userProfile.getFileById(fileKey);
			if (toDelete == null) {
				logger.warn("Could not delete the file because it does not exist anymore.");
				return null;
			}

			FolderIndex parent = toDelete.getParent();
			if (parent == null) {
				logger.error("Got task to delete the root, which is invalid.");
				return null;
			}

			// check write permision
			if (!parent.canWrite(sender)) {
				logger.error("User without WRITE permissions tried to delete a file.");
				return null;
			}

			parent.removeChild(toDelete);

			try {
				profileManager.readyToPut(userProfile, randomPID);
				logger.debug("Removed the dead link from the user profile.");
			} catch (VersionForkAfterPutException e) {
				continue;
			}

			break;
		}
		return toDelete;
	}

	/**
	 * Removes the file from the disk
	 * 
	 * @param fileManager
	 * @param toDelete the {@link FolderIndex} to remove
	 */
	private void removeFileOnDisk(File root, Index toDelete) {
		Path path = null; // = FileUtil.getPath(root, toDelete);

		if (path == null) {
			logger.error("Could not find the file to delete.");
		}
		File file = path.toFile();
		if (!file.exists()) {
			logger.error("File does not exist and cannot be deleted.");
		}

		try {
			networkManager.getEventBus().publish(new FileDeleteEvent(path, Files.isRegularFile(path)));
			Files.delete(path);
		} catch (IOException e) {
			logger.error("Could not delete file on disk.", e);
		}
	}

	/**
	 * Starts a notification process to all other clients of this very same user that received the
	 * {@link UserProfileTask}
	 * 
	 * @param toDelete the {@link FolderIndex} that has been deleted
	 * @throws InvalidProcessStateException
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	private void startNotification(Index toDelete) throws NoPeerConnectionException, InvalidProcessStateException,
			NoSessionException {
		PublicKey parentFileKey = toDelete.getParent().getFileKeys().getPublic();
		String fileName = toDelete.getName();
		DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(fileKey, parentFileKey, fileName);
		notifyOtherClients(messageFactory);
		logger.debug("Started to notify other clients about the file having been deleted by another user.");
	}

}
