package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;

/**
 * Loads the user profile, verifies write access, creates a new meta key pair for the
 * corresponding file.
 * 
 * @author Seppi
 */
public class CheckWriteAccessStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CheckWriteAccessStep.class);

	private final AddFileProcessContext context;
	private final UserProfileManager profileManager;
	private final Path root;

	public CheckWriteAccessStep(AddFileProcessContext context, UserProfileManager profileManager, Path root) {
		this.context = context;
		this.profileManager = profileManager;
		this.root = root;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.getFile();

		logger.trace(String.format("Check write access in folder '%s' to add file '%s'.", file
				.getParentFile().getName(), file.getName()));

		UserProfile userProfile = null;
		try {
			// fetch user profile (only read)
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(e);
		}

		// find the parent node using the relative path to navigate there
		FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

		// validate the write protection
		if (!parentNode.canWrite()) {
			throw new ProcessExecutionException(String.format(
					"This directory '%s' is write protected (and we don't have the keys).", file
							.getParentFile().getName()));
		}
		
		// provide the content protection keys
		context.provideProtectionKeys(parentNode.getProtectionKeys());
	}
	
	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// remove provided protection keys
		context.provideProtectionKeys(null);
	}

}
