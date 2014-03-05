package org.hive2hive.core.processes.implementations.files.delete;

import java.io.File;
import java.nio.file.Path;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

/**
 * Step that deletes a file from the index in the user profile after doing some verification.
 * 
 * @author Nico
 * 
 */
public class DeleteFromUserProfileStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFromUserProfileStep.class);

	private final DeleteFileProcessContext context;
	private final File file;
	private final UserProfileManager profileManager;
	private final Path root;

	private Index index;
	private PublicKey parentIndexKey;

	public DeleteFromUserProfileStep(File file, DeleteFileProcessContext context,
			NetworkManager networkManager) throws NoPeerConnectionException, NoSessionException {
		super(networkManager.getDataManager());
		this.file = file;
		this.context = context;
		this.profileManager = networkManager.getSession().getProfileManager();
		this.root = networkManager.getSession().getRoot();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get user profile
		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), true);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("Could not get user profile.", e);
		}

		index = profile.getFileByPath(file, root);

		// validate
		if (index == null) {
			throw new ProcessExecutionException("File index not found in user profile");
		} else if (!index.canWrite()) {
			throw new ProcessExecutionException("Not allowed to delete this file (read-only permissions)");
		}

		// check preconditions
		if (index.isFolder()) {
			FolderIndex folder = (FolderIndex) index;
			if (!folder.getChildren().isEmpty()) {
				throw new ProcessExecutionException("Cannot delete a directory that is not empty.");
			}
		}

		// remove the node from the tree
		FolderIndex parentIndex = index.getParent();
		parentIndex.removeChild(index);

		// store for later
		context.provideIndex(index);
		context.setParentNode(parentIndex);

		// store for rollback
		this.parentIndexKey = parentIndex.getFilePublicKey();

		try {
			profileManager.readyToPut(profile, getID());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException("Could not put user profile.");
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (index != null && parentIndexKey != null) {

			// get user profile
			UserProfile profile = null;
			try {
				profile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				logger.warn("Rollback failed: " + e.getMessage());
				return;
			}

			// TODO this is buggy! rather use list to check for containment instead of above if-statement
			// re-add file to user profile
			FolderIndex parent = (FolderIndex) profile.getFileById(parentIndexKey);
			parent.addChild(index);
			index.setParent(parent);

			try {
				profileManager.readyToPut(profile, getID());
			} catch (PutFailedException e) {
				logger.warn("Rollback failed: " + e.getMessage());
			}
		}
	}

}
