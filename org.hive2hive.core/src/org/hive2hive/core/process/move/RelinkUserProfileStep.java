package org.hive2hive.core.process.move;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.process.upload.UploadNotificationMessageFactory;

public class RelinkUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RelinkUserProfileStep.class);

	private boolean profileUpdated;
	private PublicKey oldParentKey = null;

	@Override
	public void start() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();

		logger.debug("Start relinking the moved file in the user profile.");

		// different possibilities of movement:
		// - file moved from root to other destination
		// - file moved from other source to root
		// - file moved from other source to other destination
		try {
			UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

			logger.debug("Start relinking the moved file in the user profile.");
			FileTreeNode movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());

			// consider renaming
			movedNode.setName(context.getDestination().getName());

			FileTreeNode oldParent = movedNode.getParent();
			oldParentKey = oldParent.getKeyPair().getPublic();

			// source's parent needs to be updated, no matter if it's root or not
			oldParent.removeChild(movedNode);

			if (context.getDestinationParentKeys() == null) {
				// moved to root
				movedNode.setParent(userProfile.getRoot());
				userProfile.getRoot().addChild(movedNode);
			} else {
				// moved to non-root
				FileTreeNode newParent = userProfile.getFileById(context.getDestinationParentKeys()
						.getPublic());
				movedNode.setParent(newParent);
				newParent.addChild(movedNode);
			}

			// update in DHT
			profileManager.readyToPut(userProfile, getProcess().getID());
			profileUpdated = true;
			logger.debug("Successfully relinked the moved file in the user profile.");

			// notify other users
			notifyUsers(context.getUsersToNotifySource(), context.getUsersToNotifyDestination(), movedNode,
					context.getSource().getName(), context.getDestination().getName());

			// done with all steps
			getProcess().setNextStep(null);
		} catch (NoSessionException | GetFailedException | PutFailedException e) {
			getProcess().stop(e);
			return;
		}
	}

	/**
	 * Sends three notification types:
	 * 1. users that have access to the file prior and after file movement
	 * 2. users that don't have access to the file anymore
	 * 3. users that now have access to the file but didn't have prior movement
	 */
	private void notifyUsers(Set<String> source, Set<String> destination, FileTreeNode movedNode,
			String sourceName, String destName) {
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

		// inform common users
		logger.debug("Inform " + common.size() + " users that a file has been moved");
		PublicKey newParentKey = movedNode.getParent().getKeyPair().getPublic();
		getProcess().notfyOtherUsers(common,
				new MoveNotificationMessageFactory(sourceName, destName, oldParentKey, newParentKey));

		// inform users that don't have access to the new destination anymore
		logger.debug("Inform " + source.size() + " users that a file has been removed (after movement)");
		source.removeAll(common);
		getProcess().notfyOtherUsers(source, new DeleteNotifyMessageFactory(oldParentKey, sourceName));

		// inform users that have now access to the moved file
		logger.debug("Inform " + destination.size() + " users that a file has been added (after movement)");
		destination.removeAll(common);
		getProcess().notfyOtherUsers(destination,
				new UploadNotificationMessageFactory(movedNode.getKeyPair().getPublic()));
	}

	@Override
	public void rollBack() {
		// only when user profile has been updated
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		if (profileUpdated) {
			try {
				UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

				// relink them
				FileTreeNode movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());
				userProfile.getRoot().removeChild(movedNode);
				FileTreeNode oldParent = userProfile.getFileById(oldParentKey);
				movedNode.setParent(oldParent);
				oldParent.addChild(movedNode);

				// update in DHT
				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (NoSessionException | GetFailedException | PutFailedException e) {
				// ignore
			}
		}

		getProcess().nextRollBackStep();
	}

}
