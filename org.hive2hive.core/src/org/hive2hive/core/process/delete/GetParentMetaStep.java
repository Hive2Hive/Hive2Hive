package org.hive2hive.core.process.delete;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

/**
 * Gets the meta folder of the parent. If the parent is root, there is no need to update it. Else, the deleted
 * document is also removed from the parent meta folder.
 * 
 * @author Nico
 * 
 */
public class GetParentMetaStep extends GetMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetParentMetaStep.class);

	private final MetaDocument metaDocumentToDelete;

	// in case of rollback
	private FileTreeNode deletedFileNode;
	private PublicKey parentKey;

	public GetParentMetaStep(MetaDocument metaDocumentToDelete) {
		super(null, null, null);
		this.metaDocumentToDelete = metaDocumentToDelete;
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		UserProfileManager profileManager = context.getProfileManager();
		UserProfile userProfile = null;
		try {
			userProfile = profileManager.getUserProfile(getProcess().getID(), true);
		} catch (GetFailedException e) {
			getProcess().stop(e.getMessage());
			return;
		}

		// update the profile here because it does not matter whether the parent meta data needs to be updated
		// or not.
		deletedFileNode = userProfile.getFileById(metaDocumentToDelete.getId());
		if (!deletedFileNode.getChildren().isEmpty()) {
			getProcess().stop("Can only delete empty directory");
			return;
		}

		FileTreeNode parent = deletedFileNode.getParent();
		parent.removeChild(deletedFileNode);
		try {
			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (PutFailedException e) {
			getProcess().stop(e.getMessage());
			return;
		}

		parentKey = parent.getKeyPair().getPublic();
		if (parent.equals(userProfile.getRoot())) {
			// no parent to update since the file is in root
			logger.debug("File is in root; skip getting the parent meta folder and notify my other clients directly");

			DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(parentKey,
					deletedFileNode.getName());
			getProcess().notifyOtherClients(messageFactory);
			getProcess().setNextStep(null);
		} else {
			// normal case when file is not in root
			logger.debug("Get the meta folder of the parent");

			super.nextStep = new UpdateParentMetaStep(metaDocumentToDelete.getId(), deletedFileNode.getName());
			super.keyPair = parent.getKeyPair();
			super.context = context;
			super.get(key2String(parent.getKeyPair().getPublic()), H2HConstants.META_DOCUMENT);
		}
	}

	@Override
	public void rollBack() {
		if (deletedFileNode != null && parentKey != null) {
			try {
				DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
				UserProfileManager profileManager = context.getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

				// add the child again to the user profile
				FileTreeNode parent = userProfile.getFileById(parentKey);
				parent.addChild(deletedFileNode);
				deletedFileNode.setParent(parent);

				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (GetFailedException | PutFailedException e) {
				// ignore during rollback
			}
		}

		getProcess().nextRollBackStep();
	}
}
