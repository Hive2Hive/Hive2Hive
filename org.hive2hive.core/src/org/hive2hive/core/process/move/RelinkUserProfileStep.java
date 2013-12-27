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
		logger.debug("Start relinking the moved file in the user profile");
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();

		try {
			UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

			// relink them
			FileTreeNode movedNode = userProfile.getFileById(context.getFileNodeKeys().getPublic());
			FileTreeNode oldParent = movedNode.getParent();
			oldParentKey = oldParent.getKeyPair().getPublic();

			oldParent.removeChild(movedNode);
			movedNode.setParent(userProfile.getRoot());
			userProfile.getRoot().addChild(movedNode);

			// update in DHT
			profileManager.readyToPut(userProfile, getProcess().getID());
			profileUpdated = true;
			logger.debug("Successfully relinked the moved file in the user profile");

			// notify other users
			notifyUsers(context.getUsersToNotifySource(), context.getUsersToNotifyDestination(),
					movedNode.getName(), movedNode.getKeyPair().getPublic());
		} catch (NoSessionException | GetFailedException | PutFailedException e) {
			getProcess().stop(e);
			return;
		}
	}

	private void notifyUsers(Set<String> source, Set<String> destination, String fileName, PublicKey fileKey) {
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
		getProcess().notfyOtherUsers(common, new MoveNotificationMessageFactory(fileKey));
		logger.debug("Inform " + common.size() + " users that a file has been moved");

		// inform users that don't have access to the new destination anymore
		source.removeAll(common);
		getProcess().notfyOtherUsers(source, new DeleteNotifyMessageFactory(oldParentKey, fileName));
		logger.debug("Inform " + source.size() + " users that a file has been removed (after movement)");

		// inform users that have now access to the moved file
		destination.removeAll(common);
		getProcess().notfyOtherUsers(destination, new UploadNotificationMessageFactory(fileKey));
		logger.debug("Inform " + destination.size() + " users that a file has been added (after movement)");
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
