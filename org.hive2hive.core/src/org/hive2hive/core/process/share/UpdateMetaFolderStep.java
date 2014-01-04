package org.hive2hive.core.process.share;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateMetaFolderStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateMetaFolderStep.class);

	private MetaFolder metaFolder;
	private KeyPair originalDomainKey;

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();

		if (context.getMetaDocument() == null) {
			getProcess()
					.stop("Meta folder does not exist, but folder is in user profile. You are in an inconsistent state");
			return;
		}

		logger.debug("Updating meta folder for sharing.");

		metaFolder = (MetaFolder) context.getMetaDocument();

		try {
			UserProfileManager profileManager = context.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
			FileTreeNode fileNode = userProfile.getFileById(metaFolder.getId());
			
			if (fileNode.isShared()) {
				getProcess().stop(new IllegalStateException("Folder is already shared."));
				return;
			}

			// store for backup
			originalDomainKey = fileNode.getDomainKeys();

			// make and put modifications
			fileNode.setDomainKeys(context.getDomainKey());
			logger.debug("Updating the domain key in the user profile");
			profileManager.readyToPut(userProfile, getProcess().getID());
			
			metaFolder.addUserPermissions(new UserPermission(context.getFriendId(), PermissionType.WRITE));

			logger.debug("Putting the modified meta folder (containing the new user permission)");
			PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFolder, new SendNotificationStep());
			getProcess().setNextStep(putMetaStep);
		} catch (GetFailedException | PutFailedException e) {
			getProcess().stop(e);
		}
	}

	@Override
	public void rollBack() {
		if (metaFolder != null) {
			// return to original domain key and put the userProfile
			ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
			UserProfileManager profileManager = context.getProfileManager();
			try {
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
				FileTreeNode fileNode = userProfile.getFileById(metaFolder.getId());
				fileNode.setDomainKeys(originalDomainKey);
				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (Exception e) {
				// ignore
			}
		}

		getProcess().nextRollBackStep();
	}

}
