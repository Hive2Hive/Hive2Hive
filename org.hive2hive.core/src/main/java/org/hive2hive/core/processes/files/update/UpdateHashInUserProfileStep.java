package org.hive2hive.core.processes.files.update;

import java.io.IOException;

import org.hive2hive.core.exceptions.AbortModificationCode;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.base.BaseModifyUserProfileStep;
import org.hive2hive.core.processes.context.UpdateFileProcessContext;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A step updating the hash in the user profile
 * 
 * @author Nico, Seppi
 */
public class UpdateHashInUserProfileStep extends BaseModifyUserProfileStep {

	private static final Logger logger = LoggerFactory.getLogger(UpdateHashInUserProfileStep.class);

	private final UpdateFileProcessContext context;

	// initialized before the user profile is modified
	private byte[] newHash;
	// set while the profile is modified, used for rollback reasons
	private byte[] originalHash;

	public UpdateHashInUserProfileStep(UpdateFileProcessContext context, UserProfileManager profileManager) {
		super(profileManager);
		this.context = context;
	}

	@Override
	protected void beforeModify() throws ProcessExecutionException {
		try {
			newHash = HashUtil.hash(context.consumeFile());
		} catch (IOException e) {
			throw new ProcessExecutionException(this, "The new hash for the user profile could not be generated.");
		}
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		BaseMetaFile metaFile = context.consumeMetaFile();
		FileIndex index = (FileIndex) userProfile.getFileById(metaFile.getId());

		// store hash of meta file
		index.setMetaFileHash(context.consumeHash());

		// store for backup
		originalHash = index.getHash();
		if (HashUtil.compare(originalHash, newHash)) {
			throw new AbortModifyException(AbortModificationCode.SAME_CONTENT,
					"Try to create new version with same content.");
		}

		// make modifications
		logger.debug("Updating the hash in the user profile.");
		index.setHash(newHash);

		// store for notification
		context.provideIndex(index);
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		BaseMetaFile metaFile = context.consumeMetaFile();
		FileIndex fileNode = (FileIndex) userProfile.getFileById(metaFile.getId());
		fileNode.setHash(originalHash);
	}
}
