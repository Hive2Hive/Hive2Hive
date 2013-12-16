package org.hive2hive.core.process.move;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateSourceParentStep extends PutMetaDocumentStep {

	private boolean profileUpdated;

	public UpdateSourceParentStep() {
		super(null, null);
		profileUpdated = false;
	}

	@Override
	public void start() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		MetaDocument sourceParent = context.getMetaDocument();
		if (sourceParent == null) {
			getProcess().stop("Parent meta folder of source not found");
			return;
		}

		MetaFolder parent = (MetaFolder) sourceParent;
		PublicKey fileKey = context.getFileNodeKeys().getPublic();
		parent.removeChildKey(fileKey);
		super.metaDocument = parent;

		if (context.getDestinationParentKeys() == null) {
			// file is going to be in root. Next steps:
			// 1. update the user profile
			// 2. notify
			try {
				UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

				// relink them
				FileTreeNode movedNode = userProfile.getFileById(fileKey);
				FileTreeNode oldParent = movedNode.getParent();
				oldParent.removeChild(movedNode);
				movedNode.setParent(userProfile.getRoot());
				userProfile.getRoot().addChild(movedNode);

				// update in DHT
				profileManager.readyToPut(userProfile, getProcess().getID());
				profileUpdated = true;

				// TODO notify
			} catch (NoSessionException | GetFailedException | PutFailedException e) {
				getProcess().stop(e.getMessage());
				return;
			}
		} else {
			// initialize next steps:
			// 1. get parent of destination
			// 2. add the new child
			// 3. update the user profile
			// 4. notify
			super.nextStep = new GetMetaDocumentStep(context.getDestinationParentKeys(),
					new UpdateDestinationParentStep(), context);
		}

		super.start();
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
				FileTreeNode oldParent = userProfile.getFileById(metaDocument.getId());
				movedNode.setParent(oldParent);
				oldParent.addChild(movedNode);

				// update in DHT
				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (NoSessionException | GetFailedException | PutFailedException e) {
				// ignore
			}
		}

		super.rollBack();
	}
}
