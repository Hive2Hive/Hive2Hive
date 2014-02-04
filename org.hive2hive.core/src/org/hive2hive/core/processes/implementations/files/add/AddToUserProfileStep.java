package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;

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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Start updating the user profile where adding the file: " + context.getFile().getName());

		// update the user profile by adding the new file
		UserProfileManager profileManager = context.getH2HSession().getProfileManager();
		UserProfile userProfile = null;
			try {
				userProfile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				throw new ProcessExecutionException(e);
			}

			// create a file tree node in the user profile
			try {
				addFileToUserProfile(userProfile);
			} catch (IOException e) {
				throw new ProcessExecutionException("Could not add file to the user profile.", e);
			}

			try {
				profileManager.readyToPut(userProfile, getID());
			} catch (PutFailedException e) {
				throw new ProcessExecutionException(e);
			}
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
		parentKey = parentNode.getFileKey();

		// use the file keys generated in a previous step where the meta document is stored
		FileTreeNode newNode;
		if (file.isDirectory()) {
			newNode = new FileTreeNode(parentNode, metaKeyPair, file.getName());
		} else {
			byte[] md5 = EncryptionUtil.generateMD5Hash(file);
			newNode = new FileTreeNode(parentNode, metaKeyPair, file.getName(), md5);
		}

		// for later usage
		context.setNewFileTreeNode(newNode);
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
