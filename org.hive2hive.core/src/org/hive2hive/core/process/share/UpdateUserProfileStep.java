package org.hive2hive.core.process.share;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;

public class UpdateUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateUserProfileStep.class);

	private KeyPair originalDomainKey;
	private boolean modified = false;

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();

		if (context.getMetaDocument() == null) {
			getProcess()
					.stop("Meta folder does not exist, but folder is in user profile. You are in an inconsistent state");
			return;
		}

		logger.debug("Updating user profile for sharing.");

		try {
			UserProfileManager profileManager = context.getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

			FileTreeNode fileNode = userProfile.getFileById(context.getMetaDocument().getId());

			// TODO this is to restrictive, what about several users sharing one single folder?
			if (fileNode.isShared()) {
				getProcess().stop(new IllegalStateException("Folder is already shared."));
				return;
			} else if (fileNode.hasShared()) {
				logger.error("Folder contains an shared folder.");
				return;
			}

			// store for backup
			originalDomainKey = fileNode.getProtectionKeys();
			// modify
			fileNode.setProtectionKeys(context.getProtectionKeys());
			context.setFileTreeNode(fileNode);

			// upload modified profile
			logger.debug("Updating the domain key in the user profile");
			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (GetFailedException | PutFailedException e) {
			getProcess().stop(e);
			return;
		}

		// set modification flag needed for roll backs
		modified = true;

		// next step is notify all users
		getProcess().setNextStep(new SendNotificationsStep());
	}

	@Override
	public void rollBack() {
		if (modified) {
			// return to original domain key and put the userProfile
			ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
			UserProfileManager profileManager = context.getSession().getProfileManager();
			try {
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
				FileTreeNode fileNode = userProfile.getFileById(context.getMetaDocument().getId());
				fileNode.setProtectionKeys(originalDomainKey);
				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (Exception e) {
				logger.warn(String.format(
						"Rollback of updating user profile (sharing a folder) failed. exception = '%s'", e));
			}
		}
		getProcess().nextRollBackStep();
	}

}
