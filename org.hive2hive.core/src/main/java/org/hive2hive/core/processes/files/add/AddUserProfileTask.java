package org.hive2hive.core.processes.files.add;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
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
public class AddUserProfileTask extends UserProfileTask implements IUserProfileModification, IFileEventGenerator {

	private static final long serialVersionUID = -4568985873058024202L;
	private static final Logger logger = LoggerFactory.getLogger(AddUserProfileTask.class);

	private final Index addedFileIndex;
	private final PublicKey parentKey;

	public AddUserProfileTask(String sender, KeyPair protectionKeys, Index index, PublicKey parentKey) {
		super(sender, protectionKeys);
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
		UserProfileManager profileManager = session.getProfileManager();

		try {
			profileManager.modifyUserProfile(getId(), this);
		} catch (AbortModifyException | GetFailedException | PutFailedException e) {
			logger.error("Couldn't not modify the user profile", e);
			return;
		}

		try {
			// notify own other clients
			notifyOtherClients(new AddNotificationMessageFactory(networkManager.getEncryption(), addedFileIndex, parentKey));
			logger.debug("Notified other clients that a file has been updated by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the new file.", e);
		}

		// trigger event
		networkManager.getEventBus().publish(
				new FileAddEvent(addedFileIndex.asFile(session.getRootFile()), addedFileIndex.isFile()));
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
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
	}

}
