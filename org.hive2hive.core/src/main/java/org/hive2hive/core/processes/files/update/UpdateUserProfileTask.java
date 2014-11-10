package org.hive2hive.core.processes.files.update;

import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileUpdateEvent;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.IUserProfileModification;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class UpdateUserProfileTask extends UserProfileTask implements IUserProfileModification, IFileEventGenerator {

	private static final long serialVersionUID = -4568985873058024202L;

	private static final Logger logger = LoggerFactory.getLogger(UpdateUserProfileTask.class);

	private final PublicKey fileKey;

	// initialized during profile modification
	private Index updatedFile;

	public UpdateUserProfileTask(String sender, PublicKey fileKey) {
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

		UserProfileManager profileManager = session.getProfileManager();
		try {
			profileManager.modifyUserProfile(getId(), this);
		} catch (Hive2HiveException e) {
			logger.error("Couldn't load / modify / update user profile.", e);
			return;
		}

		try {
			// notify own other clients
			notifyOtherClients(new UpdateNotificationMessageFactory(updatedFile));
			logger.debug("Notified other clients that a file has been updated by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the updated file.", e);
		}

		// trigger event
		networkManager.getEventBus().publish(
				new FileUpdateEvent(updatedFile.asFile(session.getRootFile()), updatedFile.isFile()));
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		updatedFile = userProfile.getFileById(fileKey);
		if (updatedFile == null) {
			throw new AbortModifyException("Got notified about a file we don't know.");
		}

		FolderIndex parent = updatedFile.getParent();
		if (parent == null) {
			throw new AbortModifyException("Got task to update the root, which is invalid.");
		}

		// check write permission
		if (!parent.canWrite(sender)) {
			throw new AbortModifyException("User without WRITE permissions tried to update a file.");
		}

		// copy the md5 parameter of the received file
		Index existing = parent.getChildByName(updatedFile.getName());
		if (existing.isFile() && updatedFile.isFile()) {
			logger.debug("File update in a shared folder received: '{}'.", updatedFile.getName());
			FileIndex existingFile = (FileIndex) existing;
			FileIndex newFile = (FileIndex) updatedFile;
			existingFile.setMD5(newFile.getMD5());
		}
	}
}
