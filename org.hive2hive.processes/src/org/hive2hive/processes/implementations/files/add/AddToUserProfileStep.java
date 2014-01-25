package org.hive2hive.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.upload.UploadNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.AddFileProcessContext;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico
 * 
 */
public class AddToUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AddToUserProfileStep.class);

	private final AddFileProcessContext context;
	private PublicKey parentKey; // used for rollback

	public AddToUserProfileStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Start updating the user profile where adding the file: " + context.getFile().getName());

		// update the user profile by adding the new file
		UserProfileManager profileManager = context.getH2HSession().getProfileManager();
		UserProfile userProfile = null;
		try {
			userProfile = profileManager.getUserProfile(getID(), true);

			// create a file tree node in the user profile
			addFileToUserProfile(userProfile);

			profileManager.readyToPut(userProfile, getID());
		} catch (PutFailedException | GetFailedException | IOException e) {
			cancel(new RollbackReason(this, e.getMessage()));
			return;
		}

		// provide the needed data for the notification
		MetaDocument metaDocument = context.consumeMetaDocument();
		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(
				metaDocument.getId());
		context.setMessageFactory(messageFactory);
		if (context.isInRoot()) {
			// file is in root; notify only own client
			Set<String> onlyMe = new HashSet<String>(1);
			onlyMe.add(context.getH2HSession().getCredentials().getUserId());
			context.setUsers(onlyMe);
			// getProcess().sendNotification(messageFactory, onlyMe);
		} else {
			MetaFolder metaFolder = (MetaFolder) context.consumeParentMetaDocument();
			Set<String> userList = metaFolder.getUserList();
			context.setUsers(userList);
			// getProcess().sendNotification(messageFactory, userList);
		}

		logger.debug(String.format("New file process finished for file %s.", context.getFile().getName()));
	}

	/**
	 * Generates a {@link FileTreeNode} that can be added to the DHT
	 * 
	 * @param userProfile
	 * 
	 * @param file the file to be added
	 * @param fileRoot the root file of this H2HNode instance
	 * @param rootNode the root node in the tree
	 * @throws IOException
	 */
	private void addFileToUserProfile(UserProfile userProfile) throws IOException {
		Path fileRoot = context.getH2HSession().getFileManager().getRoot();
		File file = context.getFile();
		KeyPair metaKeyPair = context.getNewMetaKeyPair();

		// the parent of the new file should already exist in the tree
		Path parent = file.getParentFile().toPath();

		// find the parent node using the relative path to navigate there
		Path relativePath = fileRoot.relativize(parent);
		FileTreeNode parentNode = userProfile.getFileByPath(relativePath);
		parentKey = parentNode.getKeyPair().getPublic();

		// use the file keys generated in a previous step where the meta document is stored
		if (file.isDirectory()) {
			new FileTreeNode(parentNode, metaKeyPair, file.getName());
		} else {
			byte[] md5 = EncryptionUtil.generateMD5Hash(file);
			new FileTreeNode(parentNode, metaKeyPair, file.getName(), md5);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// remove the file from the user profile
		UserProfileManager profileManager = context.getH2HSession().getProfileManager();

		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FileTreeNode parentNode = userProfile.getFileById(parentKey);
			FileTreeNode childNode = parentNode.getChildByName(context.getFile().getName());
			parentNode.removeChild(childNode);
			profileManager.readyToPut(userProfile, getID());
		} catch (Exception e) {
			// ignore
		}
	}
}
