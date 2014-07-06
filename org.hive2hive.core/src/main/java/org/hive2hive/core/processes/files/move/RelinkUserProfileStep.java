package org.hive2hive.core.processes.files.move;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.InitializeMetaUpdateStep;
import org.hive2hive.core.processes.context.MoveFileProcessContext;
import org.hive2hive.core.processes.context.MoveUpdateProtectionKeyContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.AddNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.DeleteNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.MoveNotificationContext;
import org.hive2hive.core.processes.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.files.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelinkUserProfileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(RelinkUserProfileStep.class);

	private final MoveFileProcessContext context;
	private final NetworkManager networkManager;

	private boolean profileUpdated;
	private PublicKey oldParentKey = null;

	public RelinkUserProfileStep(MoveFileProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Start relinking the moved file in the user profile.");

		// different possibilities of movement:
		// - file moved from root to other destination
		// - file moved from other source to root
		// - file moved from other source to other destination
		// Additionally, the file can be renamed (within any directory)
		try {
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);

			logger.debug("Start relinking the moved file in the user profile.");
			Index movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());

			// consider renaming
			movedNode.setName(context.getDestination().getName());

			FolderIndex oldParent = movedNode.getParent();
			oldParentKey = oldParent.getFileKeys().getPublic();

			// get the new parent
			FolderIndex newParent = (FolderIndex) userProfile.getFileByPath(context.getDestination().getParentFile(),
					networkManager.getSession().getRoot());

			// validate
			if (!oldParent.canWrite()) {
				throw new ProcessExecutionException("No write access to the source directory");
			} else if (!newParent.canWrite()) {
				throw new ProcessExecutionException("No write access to the destination directory");
			}

			// source's parent needs to be updated, no matter if it's root or not
			oldParent.removeChild(movedNode);

			// relink them
			movedNode.setParent(newParent);
			newParent.addChild(movedNode);

			// update in DHT
			profileManager.readyToPut(userProfile, getID());
			profileUpdated = true;
			logger.debug("Successfully relinked the moved file in the user profile.");

			// check if the protection key needs to be updated
			if (!H2HDefaultEncryption.compare(oldParent.getProtectionKeys(), newParent.getProtectionKeys())) {
				// update the protection key of the meta file and eventually all chunks
				logger.info("Required to update the protection key of the moved file(s)/folder(s).");
				initPKUpdateStep(movedNode, oldParent.getProtectionKeys(), newParent.getProtectionKeys());
			}

			// notify other users
			initNotificationParameters(oldParent.getCalculatedUserList(), movedNode);

		} catch (NoSessionException | GetFailedException | PutFailedException | NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}
	}

	private void initPKUpdateStep(Index movedNode, KeyPair oldProtectionKeys, KeyPair newProtectionKeys)
			throws NoPeerConnectionException {
		MoveUpdateProtectionKeyContext pkUpdateContext = new MoveUpdateProtectionKeyContext(movedNode, oldProtectionKeys,
				newProtectionKeys);
		getParent().insertNext(new InitializeMetaUpdateStep(pkUpdateContext, networkManager.getDataManager()), this);
	}

	/**
	 * Sends three notification types:
	 * 1. users that have access to the file prior and after file movement
	 * 2. users that don't have access to the file anymore
	 * 3. users that now have access to the file but didn't have prior movement
	 */
	private void initNotificationParameters(Set<String> usersAtSource, Index movedNode) {
		// the users at the destination
		Set<String> usersAtDestination = movedNode.getCalculatedUserList();

		// add all common users to a list
		Set<String> common = new HashSet<String>();

		for (String user : usersAtSource) {
			if (usersAtDestination.contains(user)) {
				common.add(user);
			}
		}

		for (String user : usersAtDestination) {
			if (usersAtSource.contains(user)) {
				common.add(user);
			}
		}

		// convenience fields
		PublicKey fileKey = movedNode.getFilePublicKey();
		String sourceName = context.getSource().getName();
		String destName = context.getDestination().getName();

		// inform common users
		logger.debug("Inform {} users that a file has been moved.", common.size());
		PublicKey newParentKey = movedNode.getParent().getFilePublicKey();
		MoveNotificationContext moveContext = context.getMoveNotificationContext();
		moveContext.provideMessageFactory(new MoveNotificationMessageFactory(sourceName, destName, oldParentKey,
				newParentKey));
		moveContext.provideUsersToNotify(common);

		// inform users that don't have access to the new destination anymore
		logger.debug("Inform {} users that a file has been removed (after movement).", usersAtSource.size());
		usersAtSource.removeAll(common);
		DeleteNotificationContext deleteContext = context.getDeleteNotificationContext();
		deleteContext.provideMessageFactory(new DeleteNotifyMessageFactory(fileKey, oldParentKey, sourceName));
		deleteContext.provideUsersToNotify(usersAtSource);

		// inform users that have now access to the moved file
		logger.debug("Inform {} users that a file has been added (after movement).", usersAtDestination.size());
		usersAtDestination.removeAll(common);
		AddNotificationContext addContext = context.getAddNotificationContext();
		addContext.provideMessageFactory(new UploadNotificationMessageFactory(movedNode, movedNode.getParent()
				.getFilePublicKey()));
		addContext.provideUsersToNotify(usersAtDestination);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// only when user profile has been updated
		if (profileUpdated) {
			try {
				UserProfileManager profileManager = networkManager.getSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);

				// relink them
				Index movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());
				userProfile.getRoot().removeChild(movedNode);
				FolderIndex oldParent = (FolderIndex) userProfile.getFileById(oldParentKey);
				movedNode.setParent(oldParent);
				oldParent.addChild(movedNode);

				// update in DHT
				profileManager.readyToPut(userProfile, getID());
			} catch (NoSessionException | GetFailedException | PutFailedException e) {
				logger.error("Rollbacking a step failed.", e);
			}
		}
	}
}
