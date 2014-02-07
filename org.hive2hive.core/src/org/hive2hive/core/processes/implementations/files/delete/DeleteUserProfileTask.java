package org.hive2hive.core.processes.implementations.files.delete;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.IndexNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;

/**
 * {@link UserProfileTask} that is pushed into the queue when a shared file is deleted. It removes the dead
 * reference in the user profile of this user and also removes the file on disk.
 * 
 * @author Nico
 * 
 */
public class DeleteUserProfileTask extends UserProfileTask {

	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteUserProfileTask.class);

	private static final long serialVersionUID = 4580106953301162049L;
	private final PublicKey fileKey;

	public DeleteUserProfileTask(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public void start() {
		try {
			H2HSession session = networkManager.getSession();

			// remove dead link from user profile
			IndexNode toDelete = updateUserProfile(session.getProfileManager());

			if (toDelete == null)
				return;

			// remove the file on disk
			removeFileOnDisk(session.getFileManager(), toDelete);

			// notify others
			startNotification(toDelete);
		} catch (Hive2HiveException e) {
			logger.error("Could not execute the task", e);
		}
	}

	/**
	 * Removes the {@link IndexNode} in the user profile
	 * 
	 * @param profileManager
	 * @return the removed node
	 */
	private IndexNode updateUserProfile(UserProfileManager profileManager) throws GetFailedException,
			PutFailedException {
		String randomPID = UUID.randomUUID().toString();

		UserProfile userProfile = profileManager.getUserProfile(randomPID, true);
		IndexNode toDelete = userProfile.getFileById(fileKey);
		if (toDelete == null) {
			logger.warn("Could not delete the file because it does not exist anymore");
			return null;
		}

		IndexNode parent = toDelete.getParent();
		if (parent == null) {
			logger.error("Got task to delete the root, which is invalid");
			return null;
		}

		parent.removeChild(toDelete);
		profileManager.readyToPut(userProfile, randomPID);
		logger.debug("Removed the dead link from the user profile");
		return toDelete;
	}

	/**
	 * Removes the file from the disk
	 * 
	 * @param fileManager
	 * @param toDelete the {@link IndexNode} to remove
	 */
	private void removeFileOnDisk(FileManager fileManager, IndexNode toDelete) {
		Path path = fileManager.getPath(toDelete);
		if (path == null) {
			logger.error("Could not find the file to delete");
		}
		File file = path.toFile();
		if (!file.exists()) {
			logger.error("File does not exist and cannot be deleted");
		}

		try {
			Files.delete(path);
		} catch (IOException e) {
			logger.error("Could not delete file on disk", e);
		}
	}

	/**
	 * Starts a notification process to all other clients of this very same user that received the
	 * {@link UserProfileTask}
	 * 
	 * @param toDelete the {@link IndexNode} that has been deleted
	 * @throws InvalidProcessStateException
	 * @throws NoPeerConnectionException
	 * @throws IllegalArgumentException
	 */
	private void startNotification(IndexNode toDelete) throws IllegalArgumentException,
			NoPeerConnectionException, InvalidProcessStateException {
		PublicKey parentFileKey = toDelete.getParent().getKeyPair().getPublic();
		String fileName = toDelete.getName();
		DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(fileKey, parentFileKey,
				fileName);
		notifyOtherClients(messageFactory);
		logger.debug("Started to notify other clients that the file has been deleted by another user");
	}

}
