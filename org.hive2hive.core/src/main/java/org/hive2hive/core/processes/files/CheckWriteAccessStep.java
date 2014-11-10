package org.hive2hive.core.processes.files;

import java.io.File;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.interfaces.IUploadContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ParentInUserProfileNotFoundException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the user profile, verifies write access, creates a new meta key pair for the
 * corresponding file.
 * 
 * @author Seppi
 */
public class CheckWriteAccessStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(CheckWriteAccessStep.class);

	private final IUploadContext context;
	private final UserProfileManager profileManager;

	public CheckWriteAccessStep(IUploadContext context, UserProfileManager profileManager) {
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		File root = context.consumeRoot();

		logger.trace("Check write access in folder '{}' to add file '{}'.", file.getParentFile().getName(), file.getName());

		UserProfile userProfile = null;
		try {
			// fetch user profile (only read)
			userProfile = profileManager.readUserProfile();
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(e);
		}

		// find the parent node using the relative path to navigate there
		FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

		if (parentNode == null) {
			throw new ParentInUserProfileNotFoundException("parentNode == null");
		}
		// validate the write protection
		if (!parentNode.canWrite()) {
			throw new ProcessExecutionException(String.format(
					"This directory '%s' is write protected (and we don't have the keys).", file.getParentFile().getName()));
		}

		// provide the content protection keys, use same for chunks and meta file
		context.provideChunkProtectionKeys(parentNode.getProtectionKeys());
		context.provideMetaFileProtectionKeys(parentNode.getProtectionKeys());
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// remove provided protection keys
		context.provideChunkProtectionKeys(null);
		context.provideMetaFileProtectionKeys(null);
	}

}
