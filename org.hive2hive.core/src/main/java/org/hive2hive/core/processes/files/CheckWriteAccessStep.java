package org.hive2hive.core.processes.files;

import java.io.File;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.ParentInUserProfileNotFoundException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.interfaces.IUploadContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the user profile, verifies write access, creates a new meta key pair for the
 * corresponding file.
 * 
 * @author Seppi
 */
public class CheckWriteAccessStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CheckWriteAccessStep.class);

	private final IUploadContext context;
	private final UserProfileManager profileManager;

	public CheckWriteAccessStep(IUploadContext context, UserProfileManager profileManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		File root = context.consumeRoot();

		logger.trace("Check write access in folder '{}' to add file '{}'.", file.getParentFile().getName(), file.getName());

		UserProfile userProfile = null;
		try {
			// fetch user profile (only read)
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		// find the parent node using the relative path to navigate there
		FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

		if (parentNode == null) {
			throw new ProcessExecutionException(this, new ParentInUserProfileNotFoundException("parentNode == null"));
		}
		// validate the write protection
		if (!parentNode.canWrite()) {
			throw new ProcessExecutionException(this, String.format(
					"The directory '%s' is write protected (and we don't have the keys).", file.getParentFile().getName()));
		}

		// provide the content protection keys, use same for chunks and meta file
		context.provideChunkProtectionKeys(parentNode.getProtectionKeys());
		context.provideMetaFileProtectionKeys(parentNode.getProtectionKeys());
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		// remove provided protection keys
		context.provideChunkProtectionKeys(null);
		context.provideMetaFileProtectionKeys(null);
		setRequiresRollback(false);
		return null;
	}

}
