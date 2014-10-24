package org.hive2hive.core.processes.files.add;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico, Seppi
 */
public class AddIndexToUserProfileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(AddIndexToUserProfileStep.class);

	private final AddFileProcessContext context;
	private final UserProfileManager profileManager;

	// used for rollback
	private PublicKey parentKey;
	private boolean modified = false;

	public AddIndexToUserProfileStep(AddFileProcessContext context, UserProfileManager profileManager) {
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		Path root = context.consumeRoot();

		// pre-calculate the meta keys because this may take a while
		KeyPair metaKeys = context.generateOrGetMetaKeys();

		// pre-calculate the md5 hash because this may take a while
		byte[] md5 = null;
		if (file.isFile()) {
			md5 = calculateHash(file);
		}

		// update the user profile where adding the file
		while (true) {
			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				throw new ProcessExecutionException(e);
			}

			// find the parent node using the relative path to navigate there
			FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

			// validate the write protection
			if (!parentNode.canWrite()) {
				throw new ProcessExecutionException("This directory is write protected (and we don't have the keys).");
			}

			// create a file tree node in the user profile
			parentKey = parentNode.getFilePublicKey();
			// use the file keys generated above is stored
			if (file.isDirectory()) {
				FolderIndex folderIndex = new FolderIndex(parentNode, metaKeys, file.getName());
				context.provideIndex(folderIndex);
			} else {
				FileIndex fileIndex = new FileIndex(parentNode, metaKeys, file.getName(), md5);
				context.provideIndex(fileIndex);
			}

			try {
				// put the updated user profile
				profileManager.readyToPut(userProfile, getID());

				// set flag, that an update has been made (for roll back)
				modified = true;
			} catch (VersionForkAfterPutException e) {
				// repeat user profile modification
				continue;
			} catch (PutFailedException e) {
				throw new ProcessExecutionException(e);
			}

			break;
		}
	}

	private byte[] calculateHash(File file) throws ProcessExecutionException {
		try {
			return HashUtil.hash(file);
		} catch (IOException e) {
			logger.error("Creating MD5 hash of file '{}' was not possible.", file.getName(), e);
			throw new ProcessExecutionException(
					String.format("Could not add file '%s' to the user profile.", file.getName()), e);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (modified) {
			// remove the file from the user profile
			while (true) {
				UserProfile userProfile;
				try {
					userProfile = profileManager.getUserProfile(getID(), true);
				} catch (GetFailedException e) {
					logger.warn("Couldn't load user profile for redo.", e);
					return;
				}

				// find the parent and child node
				FolderIndex parentNode = (FolderIndex) userProfile.getFileById(parentKey);
				Index childNode = parentNode.getChildByName(context.consumeFile().getName());

				// remove newly added child node
				parentNode.removeChild(childNode);

				try {
					// put the user profile
					profileManager.readyToPut(userProfile, getID());

					// adapt flag
					modified = false;
				} catch (VersionForkAfterPutException e) {
					// repeat user profile modification
					continue;
				} catch (PutFailedException e) {
					logger.warn("Couldn't redo put of user profile.", e);
					return;
				}
				break;
			}
		}
	}
}
