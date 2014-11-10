package org.hive2hive.core.processes.files.update;

import java.io.IOException;

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
 * A step updating the MD5 hash in the user profile
 * 
 * @author Nico, Seppi
 */
public class UpdateMD5inUserProfileStep extends BaseModifyUserProfileStep {

	private static final Logger logger = LoggerFactory.getLogger(UpdateMD5inUserProfileStep.class);

	private final UpdateFileProcessContext context;

	// initialized before the user profile is modified
	private byte[] newMD5;
	// set while the profile is modified, used for rollback reasons
	private byte[] originalMD5;

	public UpdateMD5inUserProfileStep(UpdateFileProcessContext context, UserProfileManager profileManager) {
		super(profileManager);
		this.context = context;
	}

	@Override
	protected void beforeModify() throws ProcessExecutionException {
		try {
			newMD5 = HashUtil.hash(context.consumeFile());
		} catch (IOException e) {
			throw new ProcessExecutionException("The new MD5 hash for the user profile could not be generated.", e);
		}
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		BaseMetaFile metaFile = context.consumeMetaFile();
		FileIndex index = (FileIndex) userProfile.getFileById(metaFile.getId());

		// store hash of meta file
		index.setMetaFileHash(context.consumeHash());

		// store for backup
		originalMD5 = index.getMD5();
		if (HashUtil.compare(originalMD5, newMD5)) {
			throw new AbortModifyException("Try to create new version with same content.");
		}

		// make and put modifications
		index.setMD5(newMD5);
		logger.debug("Updating the MD5 hash in the user profile.");
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		BaseMetaFile metaFile = context.consumeMetaFile();
		FileIndex fileNode = (FileIndex) userProfile.getFileById(metaFile.getId());
		fileNode.setMD5(originalMD5);
	}
}
