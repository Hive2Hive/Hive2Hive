package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.IUserProfileModification;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
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

		DeleteUPModification modification = new DeleteUPModification(session.getRootFile());
		UserProfileManager profileManager = session.getProfileManager();
		try {
			profileManager.modifyUserProfile(getId(), modification);
		} catch (Hive2HiveException e) {
			logger.error("Couldn't update / modify the user profile.", e);
			return;
		}

		try {
			// notify own other clients
			notifyOtherClients(modification.getMessageFactory());
			logger.debug("Notified other clients that a file has been deleted by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the deleted file.", e);
		}

		// trigger event
		networkManager.getEventBus().publish(modification.getFileDeleteEvent());
	}

	private class DeleteUPModification implements IUserProfileModification {

		private final File root;
		private BaseNotificationMessageFactory messageFactory;
		private FileDeleteEvent fileDeleteEvent;

		public DeleteUPModification(File root) {
			this.root = root;
		}

		@Override
		public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
			Index fileToDelete = userProfile.getFileById(fileKey);
			if (fileToDelete == null) {
				throw new AbortModifyException("Got notified about a file we don't know.");
			}

			FolderIndex parent = fileToDelete.getParent();
			if (parent == null) {
				throw new AbortModifyException("Got task to delete the root, which is invalid.");
			}

			// check write permission
			if (!parent.canWrite(sender)) {
				throw new AbortModifyException("User without WRITE permissions tried to delete a file.");
			}

			parent.removeChild(fileToDelete);

			// prepare objects for notification if the UP modification was successful
			messageFactory = new DeleteNotifyMessageFactory(fileToDelete.getFilePublicKey(), parent.getFilePublicKey(),
					fileToDelete.getName(), fileToDelete.isFile());
			fileDeleteEvent = new FileDeleteEvent(fileToDelete.asFile(root), fileToDelete.isFile());
		}

		public BaseNotificationMessageFactory getMessageFactory() {
			return messageFactory;
		}

		public FileDeleteEvent getFileDeleteEvent() {
			return fileDeleteEvent;
		}
	}

}