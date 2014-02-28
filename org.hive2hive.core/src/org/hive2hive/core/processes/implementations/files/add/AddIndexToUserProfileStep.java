package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
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
 * @author Nico, Seppi
 */
public class AddIndexToUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AddIndexToUserProfileStep.class);

	private final AddFileProcessContext context;
	private final UserProfileManager profileManager;
	private final Path root;

	private PublicKey parentKey; // used for rollback
	private boolean modified = false;

	public AddIndexToUserProfileStep(AddFileProcessContext context, UserProfileManager profileManager,
			Path root) {
		this.context = context;
		this.profileManager = profileManager;
		this.root = root;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// first generate the new key pair for the meta file (which are stored to the context)
		logger.debug("Create meta file keys for the new file: " + context.getFile().getName());
		KeyPair metaKeyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		context.setNewMetaKeyPair(metaKeyPair);

		File file = context.getFile();
		logger.debug("Start updating the user profile where adding the file: " + file.getName());
		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);

			// create a file tree node in the user profile
			// find the parent node using the relative path to navigate there
			FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

			// validate the write protection
			if (!parentNode.canWrite()) {
				throw new ProcessExecutionException(
						"This directory is write protected (and we don't have the keys).");
			}

			parentKey = parentNode.getFilePublicKey();
			// use the file keys generated above is stored
			if (file.isDirectory()) {
				context.provideIndex(new FolderIndex(parentNode, metaKeyPair, file.getName()));
			} else {
				byte[] md5 = EncryptionUtil.generateMD5Hash(file);
				context.provideIndex(new FileIndex(parentNode, metaKeyPair, file.getName(), md5));
			}

			// put the updated user profile
			profileManager.readyToPut(userProfile, getID());
			modified = true;
		} catch (IOException e) {
			logger.error("Creating MD5 hash of file " + file.getName() + " was not possible. Reason: "
					+ e.getMessage());
			throw new ProcessExecutionException("Could not add file to the user profile.", e);
		} catch (PutFailedException | GetFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (modified) {
			// remove the file from the user profile
			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				return;
			}
			FolderIndex parentNode = (FolderIndex) userProfile.getFileById(parentKey);
			Index childNode = parentNode.getChildByName(context.getFile().getName());
			parentNode.removeChild(childNode);
			try {
				profileManager.readyToPut(userProfile, getID());
			} catch (PutFailedException e) {
				return;
			}
			modified = false;
		}
	}
}
