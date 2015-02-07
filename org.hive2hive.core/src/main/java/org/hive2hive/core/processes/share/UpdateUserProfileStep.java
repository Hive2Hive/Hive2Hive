package org.hive2hive.core.processes.share;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.common.base.BaseModifyUserProfileStep;
import org.hive2hive.core.processes.context.ShareProcessContext;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the corresponding {@link FolderIndex} in the {@link UserProfile}. Sets content protection keys,
 * user permissions and the share flag.
 * 
 * @author Nico, Seppi
 */
public class UpdateUserProfileStep extends BaseModifyUserProfileStep {

	private static final Logger logger = LoggerFactory.getLogger(UpdateUserProfileStep.class);

	private final ShareProcessContext context;
	private final File root;
	private final String userId;
	private final IH2HEncryption encryption;

	private KeyPair newProtectionKeys;

	public UpdateUserProfileStep(ShareProcessContext context, H2HSession session, IH2HEncryption encryption)
			throws NoSessionException {
		super(session.getProfileManager());
		this.context = context;
		this.encryption = encryption;
		this.root = session.getRootFile();
		this.userId = session.getUserId();
	}

	@Override
	protected void beforeModify() throws ProcessExecutionException {
		// generate the new key pair only once
		newProtectionKeys = encryption.generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION);
		// make it available for future steps where we change protection keys
		context.provideNewProtectionKeys(newProtectionKeys);
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		FolderIndex folderIndex = (FolderIndex) userProfile.getFileByPath(context.getFolder(), root);

		if (!folderIndex.canWrite()) {
			throw new AbortModifyException(String.format("Cannot share folder '%s' with read-only access.",
					folderIndex.getName()));
		} else if (!folderIndex.getSharedFlag() && folderIndex.isSharedOrHasSharedChildren()) {
			// restriction that disallows sharing folders within other shared folders
			throw new AbortModifyException(String.format("Folder '%s' is already shared or contains an shared folder.",
					folderIndex.getName()));
		}

		// check if the folder is already shared with this user
		if (folderIndex.getCalculatedUserList().contains(context.getFriendId())) {
			throw new AbortModifyException(String.format("Friend '%s' already has access to folder '%s'.",
					context.getFriendId(), folderIndex.getName()));
		}

		// store for the notification
		context.provideIndex(folderIndex);

		if (folderIndex.getSharedFlag()) {
			// this if-clause allows sharing with multiple users and omits the next if-clause
			logger.debug("Sharing an already shared folder '{}' with friend '{}'.", folderIndex.getName(),
					context.getFriendId());
			folderIndex.addUserPermissions(context.getUserPermission());
		} else {
			// make the node shared with the new protection keys
			folderIndex.share(newProtectionKeys);
			// add read/write user permission of friend
			folderIndex.addUserPermissions(context.getUserPermission());
			// add write user permission of user itself
			folderIndex.addUserPermissions(new UserPermission(userId, PermissionType.WRITE));
		}
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		FolderIndex folderNode = (FolderIndex) userProfile.getFileById(context.consumeMetaFile().getId());
		// unshare the fileNode
		folderNode.unshare();
	}
}
