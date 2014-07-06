package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.nio.file.Path;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.File2MetaFileComponent;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step that deletes a file from the index in the user profile after doing some verification.
 * In case the deleted file is a file (and not a folder), this step initiates the deletion of the meta file
 * and all according chunks.
 * 
 * @author Nico
 * 
 */
public class DeleteFromUserProfileStep extends BaseGetProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(DeleteFromUserProfileStep.class);

	private final DeleteFileProcessContext context;
	private final UserProfileManager profileManager;
	private DataManager dataManager;
	private final Path root;

	private Index index;
	private PublicKey parentIndexKey;

	public DeleteFromUserProfileStep(DeleteFileProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException, NoSessionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.dataManager = networkManager.getDataManager();
		this.profileManager = networkManager.getSession().getProfileManager();
		this.root = networkManager.getSession().getRoot();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();

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

		// store for rollback
		this.parentIndexKey = parentIndex.getFilePublicKey();

		try {
			profileManager.readyToPut(profile, getID());
		} catch (PutFailedException e) {
			logger.error("Cannot remove the file {} from the user profile", index.getFullPath(), e);
			throw new ProcessExecutionException("Could not put user profile.");
		}

		if (index.isFile()) {
			/**
			 * Delete the meta file and all chunks
			 */
			File2MetaFileComponent file2Meta = new File2MetaFileComponent(index, context, dataManager);
			DeleteChunksProcess deleteChunks = new DeleteChunksProcess(context, dataManager);
			DeleteMetaFileStep deleteMeta = new DeleteMetaFileStep(context, dataManager);

			// insert them in correct order
			getParent().insertNext(file2Meta, this);
			getParent().insertNext(deleteChunks, file2Meta);
			getParent().insertNext(deleteMeta, deleteChunks);
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
				logger.warn("Rollback failed.", e);
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
				logger.warn("Rollback failed.", e);
			}
		}
	}

}
