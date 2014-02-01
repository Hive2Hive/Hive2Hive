package org.hive2hive.processes.implementations.files.delete;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.util.HashSet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.processes.implementations.context.DeleteFileProcessContext;

public class DeleteFromUserProfileStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFromUserProfileStep.class);

	private final DeleteFileProcessContext context;
	private final NetworkManager networkManager;

	private FileTreeNode fileNode;
	private FileTreeNode parentNode;

	public DeleteFromUserProfileStep(DeleteFileProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "No meta document given."));
			return;
		}

		// get user profile
		UserProfile profile = null;
		try {
			profile = networkManager.getSession().getProfileManager().getUserProfile(getID(), true);
		} catch (GetFailedException | NoSessionException e) {
			cancel(new RollbackReason(this, "Could not get user profile."));
			return;
		}

		fileNode = profile.getFileById(context.consumeMetaDocument().getId());
		context.setChildNode(fileNode);

		// check preconditions
		if (!fileNode.getChildren().isEmpty()) {
			cancel(new RollbackReason(this, "Cannot delete a directory that is not empty."));
			return;
		}

		parentNode = fileNode.getParent();
		parentNode.removeChild(fileNode);
		context.setParentNode(parentNode);
		try {
			networkManager.getSession().getProfileManager().readyToPut(profile, getID());
		} catch (PutFailedException | NoSessionException e) {
			cancel(new RollbackReason(this, "Could not put user profile."));
		}

		if (parentNode.equals(profile.getRoot())) {
			context.setIsInRootFile(true);
			notifyOtherClients(fileNode, parentNode, profile);

		} else {
			// next step done because of flag
			context.setIsInRootFile(false);
			loadParentMetaFolder();
		}
	}

	private void notifyOtherClients(FileTreeNode file, FileTreeNode parent, UserProfile profile) {
		logger.debug(String
				.format("File '%s' is in root, skip getting the parent meta folder and notify my other clients directly.",
						file.getName()));

		DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(file.getFileKey(), parent
				.getKeyPair().getPublic(), file.getName());
		HashSet<String> users = new HashSet<String>();
		users.add(profile.getUserId());

		// TODO send notification
		// getProcess().sendNotification(messageFactory, users);
	}

	private void loadParentMetaFolder() throws InvalidProcessStateException {

		// get the parent meta document
		FileTreeNode parent = context.getParentNode();
		NetworkContent loadedContent = get(H2HEncryptionUtil.key2String(parent.getKeyPair().getPublic()),
				H2HConstants.META_DOCUMENT);

		if (loadedContent == null) {
			cancel(new RollbackReason(this, "Parent meta folder not found."));
		} else {

			// decrypt parent meta folder
			HybridEncryptedContent encryptedContent = (HybridEncryptedContent) loadedContent;

			PrivateKey decryptionKey = parent.getKeyPair().getPrivate();

			NetworkContent decryptedContent = null;
			try {
				decryptedContent = H2HEncryptionUtil.decryptHybrid(encryptedContent, decryptionKey);
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| ClassNotFoundException | IOException e) {
				cancel(new RollbackReason(this, "Parent meta document could not be decrypted. Reason: "
						+ e.getMessage()));
				return;
			}

			MetaFolder parentMetaFolder = (MetaFolder) decryptedContent;
			parentMetaFolder.setVersionKey(loadedContent.getVersionKey());
			parentMetaFolder.setBasedOnKey(loadedContent.getBasedOnKey());

			context.setParentMetaFolder(parentMetaFolder);
			context.setParentProtectionKeys(parent.getProtectionKeys());
			context.setEncryptedParentMetaFolder(encryptedContent);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		if (fileNode != null && parentNode != null) {

			// get user profile
			UserProfile profile = null;
			try {
				profile = networkManager.getSession().getProfileManager().getUserProfile(getID(), true);
			} catch (GetFailedException | NoSessionException e) {
				logger.warn("Rollback failed: " + e.getMessage());
				return;
			}

			// TODO this is buggy! rather use list to check for containment instead of above if-statement
			// re-add file to user profile
			FileTreeNode parent = profile.getFileById(parentNode.getFileKey());
			parent.addChild(fileNode);
			fileNode.setParent(parent);

			try {
				networkManager.getSession().getProfileManager().readyToPut(profile, getID());
			} catch (PutFailedException | NoSessionException e) {
				logger.warn("Rollback failed: " + e.getMessage());
				return;
			}
		}
	}

}
