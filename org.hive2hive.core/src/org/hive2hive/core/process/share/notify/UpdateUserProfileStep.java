package org.hive2hive.core.process.share.notify;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;

/**
 * A step adding the new shared folder (node) into the user profile (tree)
 * 
 * @author Seppi
 * 
 */
public class UpdateUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateUserProfileStep.class);

	@Override
	public void start() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess().getContext();
		logger.debug("Start updating the user profile where adding the shared folder.");

		// update the user profile by adding the new file
		UserProfileManager profileManager = context.getProfileManager();
		UserProfile userProfile = null;
		try {
			userProfile = profileManager.getUserProfile(getProcess().getID(), true);
			
			FileTreeNode fileTreeNode = context.getFileTreeNode();
			
			// assign the newly received subtree to the user profile
			fileTreeNode.setParent(userProfile.getRoot());
			userProfile.getRoot().addChild(fileTreeNode);

			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (PutFailedException | GetFailedException e) {
			getProcess().stop(e);
			return;
		}

		getProcess().setNextStep(new SynchronizeSharedFolderStep());
	}

	@Override
	public void rollBack() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess().getContext();

		try {
			UserProfileManager profileManager = context.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

			// remove the newly received shared folder (file tree node) from the user profile
			FileTreeNode rootNode = userProfile.getRoot();
			rootNode.removeChild(context.getFileTreeNode());
			
			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (Exception e) {
			// ignore
		}

		getProcess().nextRollBackStep();
	}
}
