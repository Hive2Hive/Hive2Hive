package org.hive2hive.core.processes.files.add;

import java.io.File;
import java.io.IOException;

import org.hive2hive.core.exceptions.AbortModificationCode;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.base.BaseModifyUserProfileStep;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico, Seppi
 */
public class AddIndexToUserProfileStep extends BaseModifyUserProfileStep {

	private static final Logger logger = LoggerFactory.getLogger(AddIndexToUserProfileStep.class);

	private final AddFileProcessContext context;

	// pre-calculated hash in case it's a file
	private byte[] hash;

	public AddIndexToUserProfileStep(AddFileProcessContext context, UserProfileManager profileManager) {
		super(profileManager);
		this.context = context;
	}

	@Override
	protected void beforeModify() throws ProcessExecutionException {
		File file = context.consumeFile();
		if (file.isFile()) {
			try {
				hash = HashUtil.hash(file);
			} catch (IOException e) {
				logger.error("Creating hash of file '{}' was not possible.", file.getName(), e);
				throw new ProcessExecutionException(this, "Cannot calculate the hash of the file " + file.getName());
			}
		}
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		File file = context.consumeFile();
		File root = context.consumeRoot();

		// find the parent node using the relative path to navigate there
		FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);

		// validate the write protection
		if (!parentNode.canWrite()) {
			throw new AbortModifyException(AbortModificationCode.NO_WRITE_PERM, "This directory is write protected (and we don't have the keys).");
		}

		// create a file tree node in the user profile
		if (file.isDirectory()) {
			FolderIndex folderIndex = new FolderIndex(parentNode, context.consumeMetaFileEncryptionKeys(), file.getName());
			context.provideIndex(folderIndex);
		} else {
			FileIndex fileIndex = new FileIndex(parentNode, context.consumeMetaFileEncryptionKeys(), file.getName(), hash);
			context.provideIndex(fileIndex);
		}
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		File file = context.consumeFile();
		File root = context.consumeRoot();

		// find the parent and child node
		FolderIndex parentNode = (FolderIndex) userProfile.getFileByPath(file.getParentFile(), root);
		Index childNode = parentNode.getChildByName(file.getName());

		// remove newly added child node
		parentNode.removeChild(childNode);
	}
}
