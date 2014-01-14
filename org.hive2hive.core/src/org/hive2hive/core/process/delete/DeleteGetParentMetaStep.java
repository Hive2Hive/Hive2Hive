package org.hive2hive.core.process.delete;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Gets the meta folder of the parent. If the parent is root, there is no need to update it. Else, the deleted
 * document is also removed from the parent meta folder.
 * 
 * @author Nico, Seppi
 */
public class DeleteGetParentMetaStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteGetParentMetaStep.class);

	private DeleteFileProcessContext context;
	private FileTreeNode deletedFileNode;
	private FileTreeNode parentFileNode;

	@Override
	public void start() {
		context = (DeleteFileProcessContext) getProcess().getContext();

		MetaDocument metaDocumentToDelete = context.getMetaDocument();
		if (metaDocumentToDelete == null) {
			getProcess().stop("Meta document to delete is null.");
			return;
		}

		UserProfile userProfile = null;
		try {
			userProfile = context.getH2HSession().getProfileManager()
					.getUserProfile(getProcess().getID(), true);

			deletedFileNode = userProfile.getFileById(metaDocumentToDelete.getId());
			if (!deletedFileNode.getChildren().isEmpty()) {
				getProcess().stop("Can only delete empty directory.");
				return;
			}

			parentFileNode = deletedFileNode.getParent();
			parentFileNode.removeChild(deletedFileNode);

			context.getH2HSession().getProfileManager().readyToPut(userProfile, getProcess().getID());
		} catch (GetFailedException | PutFailedException e) {
			getProcess().stop(e);
			return;
		}

		if (parentFileNode.equals(userProfile.getRoot())) {
			// no parent to update since the file is in root
			logger.debug(String
					.format("File '%s' is in root, skip getting the parent meta folder and notify my other clients directly.",
							deletedFileNode.getName()));

			DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(parentFileNode
					.getKeyPair().getPublic(), deletedFileNode.getName());
			getProcess().notifyOtherClients(messageFactory);

			getProcess().setNextStep(null);
		} else {
			// normal case when file is not in root
			logger.debug(String.format("Get the parent meta folder of deleted meta document of file '%s'.",
					deletedFileNode.getName()));

			get(key2String(parentFileNode.getKeyPair().getPublic()), H2HConstants.META_DOCUMENT);
		}
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			context.setParentMetaFolder(null);
			context.setParentProtectionKeys(null);
			context.setEncryptedParentMetaFolder(null);

			getProcess().stop("Parent meta folder not found.");
		} else {
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;

			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, parentFileNode
						.getKeyPair().getPrivate());
				decrypted.setVersionKey(content.getVersionKey());
				decrypted.setBasedOnKey(content.getBasedOnKey());

				context.setParentMetaFolder((MetaFolder) decrypted);
				context.setParentProtectionKeys(parentFileNode.getProtectionKeys());
				context.setEncryptedParentMetaFolder(encrypted);

				// continue with next step
				getProcess().setNextStep(new DeleteUpdateParentMetaStep(deletedFileNode.getName()));
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| IllegalArgumentException e) {
				context.setParentMetaFolder(null);
				context.setParentProtectionKeys(null);
				context.setEncryptedParentMetaFolder(null);

				getProcess().stop(e);
			}
		}
	}

	@Override
	public void rollBack() {
		if (deletedFileNode != null && parentFileNode != null) {
			try {
				DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
				UserProfileManager profileManager = context.getH2HSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);

				// add the child again to the user profile
				FileTreeNode parent = userProfile.getFileById(parentFileNode.getKeyPair().getPublic());
				parent.addChild(deletedFileNode);
				deletedFileNode.setParent(parent);

				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (GetFailedException | PutFailedException e) {
				logger.warn(String.format("Rollback of get parent meta folder failed. reason = '%s'",
						e.getMessage()));
			}
		}

		context.setParentMetaFolder(null);
		context.setParentProtectionKeys(null);

		getProcess().nextRollBackStep();
	}
}
