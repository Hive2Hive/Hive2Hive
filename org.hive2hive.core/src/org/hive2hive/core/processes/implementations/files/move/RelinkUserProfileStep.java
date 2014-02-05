package org.hive2hive.core.processes.implementations.files.move;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext.AddNotificationContext;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext.DeleteNotificationContext;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext.MoveNotificationContext;
import org.hive2hive.core.processes.implementations.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.implementations.files.delete.DeleteNotifyMessageFactory;

public class RelinkUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RelinkUserProfileStep.class);

	private final MoveFileProcessContext context;
	private final NetworkManager networkManager;

	private boolean profileUpdated;
	private PublicKey oldParentKey = null;

	public RelinkUserProfileStep(MoveFileProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Start relinking the moved file in the user profile.");

		// different possibilities of movement:
		// - file moved from root to other destination
		// - file moved from other source to root
		// - file moved from other source to other destination
		try {
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);

			logger.debug("Start relinking the moved file in the user profile.");
			FileTreeNode movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());

			// consider renaming
			movedNode.setName(context.getDestination().getName());

			FileTreeNode oldParent = movedNode.getParent();
			oldParentKey = oldParent.getKeyPair().getPublic();

			// source's parent needs to be updated, no matter if it's root or not
			oldParent.removeChild(movedNode);

			if (context.isDestinationInRoot()) {
				// moved to root
				movedNode.setParent(userProfile.getRoot());
				userProfile.getRoot().addChild(movedNode);
			} else {
				// moved to non-root
				FileTreeNode newParent = userProfile.getFileByPath(context.getDestination().getParentFile(),
						networkManager.getSession().getFileManager());
				movedNode.setParent(newParent);
				newParent.addChild(movedNode);
			}

			// update in DHT
			profileManager.readyToPut(userProfile, getID());
			profileUpdated = true;
			logger.debug("Successfully relinked the moved file in the user profile.");

			// notify other users
			initNotificationParameters(context.getUsersToNotifySource(),
					context.getUsersToNotifyDestination(), movedNode, context.getSource().getName(), context
							.getDestination().getName());

		} catch (NoSessionException | GetFailedException | PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}
	}

	/**
	 * Sends three notification types:
	 * 1. users that have access to the file prior and after file movement
	 * 2. users that don't have access to the file anymore
	 * 3. users that now have access to the file but didn't have prior movement
	 */
	private void initNotificationParameters(Set<String> source, Set<String> destination,
			FileTreeNode movedNode, String sourceName, String destName) {
		// add all common users to a list
		Set<String> common = new HashSet<String>();
		for (String user : source) {
			if (destination.contains(user))
				common.add(user);
		}

		for (String user : destination) {
			if (source.contains(user))
				common.add(user);
		}

		PublicKey fileKey = movedNode.getFileKey();

		// inform common users
		logger.debug("Inform " + common.size() + " users that a file has been moved");
		PublicKey newParentKey = movedNode.getParent().getFileKey();
		MoveNotificationContext moveContext = context.getMoveNotificationContext();
		moveContext.provideMessageFactory(new MoveNotificationMessageFactory(sourceName, destName,
				oldParentKey, newParentKey));
		moveContext.provideUsersToNotify(common);

		// inform users that don't have access to the new destination anymore
		logger.debug("Inform " + source.size() + " users that a file has been removed (after movement)");
		source.removeAll(common);
		DeleteNotificationContext deleteContext = context.getDeleteNotificationContext();
		deleteContext
				.provideMessageFactory(new DeleteNotifyMessageFactory(fileKey, oldParentKey, sourceName));
		deleteContext.provideUsersToNotify(source);

		// inform users that have now access to the moved file
		logger.debug("Inform " + destination.size() + " users that a file has been added (after movement)");
		destination.removeAll(common);
		AddNotificationContext addContext = context.getAddNotificationContext();
		addContext.provideMessageFactory(new UploadNotificationMessageFactory(movedNode, movedNode
				.getParent().getFileKey()));
		addContext.provideUsersToNotify(destination);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// only when user profile has been updated
		if (profileUpdated) {
			try {
				UserProfileManager profileManager = networkManager.getSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);

				// relink them
				FileTreeNode movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());
				userProfile.getRoot().removeChild(movedNode);
				FileTreeNode oldParent = userProfile.getFileById(oldParentKey);
				movedNode.setParent(oldParent);
				oldParent.addChild(movedNode);

				// update in DHT
				profileManager.readyToPut(userProfile, getID());
			} catch (NoSessionException | GetFailedException | PutFailedException e) {
				// ignore
			}
		}
	}
}
