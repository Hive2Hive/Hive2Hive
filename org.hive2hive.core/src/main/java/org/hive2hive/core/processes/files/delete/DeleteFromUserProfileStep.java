package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.exceptions.AbortModificationCode;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.common.base.BaseModifyUserProfileStep;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.core.processes.files.GetMetaFileStep;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Step that deletes a file from the index in the user profile after doing some verification.
 * In case the deleted file is a file (and not a folder), this step initiates the deletion of the meta file
 * and all according chunks.
 * 
 * @author Nico, Seppi
 */
public class DeleteFromUserProfileStep extends BaseModifyUserProfileStep {

	private final DeleteFileProcessContext context;
	private final DataManager dataManager;

	public DeleteFromUserProfileStep(DeleteFileProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException, NoSessionException {
		super(networkManager.getSession().getProfileManager());
		this.context = context;
		this.dataManager = networkManager.getDataManager();
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		File file = context.consumeFile();
		File root = context.consumeRoot();

		Index fileIndex = userProfile.getFileByPath(file, root);

		// validate
		if (fileIndex == null) {
			throw new AbortModifyException(AbortModificationCode.FILE_INDEX_NOT_FOUND, "File index not found in user profile");
		} else if (!fileIndex.canWrite()) {
			throw new AbortModifyException(AbortModificationCode.NO_WRITE_PERM, "Not allowed to delete this file (read-only permissions)");
		}

		// check preconditions
		if (fileIndex.isFolder()) {
			FolderIndex folder = (FolderIndex) fileIndex;
			if (!folder.getChildren().isEmpty()) {
				throw new AbortModifyException(AbortModificationCode.NON_EMPTY_DIR, "Cannot delete a directory that is not empty.");
			}
		}

		// remove the node from the tree
		FolderIndex parentIndex = fileIndex.getParent();
		parentIndex.removeChild(fileIndex);

		// store for later
		context.provideIndex(fileIndex);
	}

	@Override
	protected void afterModify() throws ProcessExecutionException {
		Index fileIndex = context.consumeIndex();
		if (fileIndex.isFile()) {
			context.provideProtectionKeys(fileIndex.getProtectionKeys());
			context.provideMetaFileEncryptionKeys(fileIndex.getFileKeys());

			// create steps to delete meta and all chunks
			GetMetaFileStep getMeta = new GetMetaFileStep(context, dataManager);
			DeleteChunksStep deleteChunks = new DeleteChunksStep(context, dataManager);
			DeleteMetaFileStep deleteMeta = new DeleteMetaFileStep(context, dataManager);

			// insert them in correct order
			List<IProcessComponent<?>> parentComponents = new ArrayList<>(getParent().getComponents());
			int index = parentComponents.indexOf(this) + 1;

			getParent().add(index, getMeta);
			getParent().add(index + 1, deleteChunks);
			getParent().add(index + 2, deleteMeta);
		}
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		File file = context.consumeFile();
		File root = context.consumeRoot();
		Index index = context.consumeIndex();

		// re-add file to user profile
		FolderIndex parent = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);
		parent.addChild(index);
		index.setParent(parent);
	}

}
