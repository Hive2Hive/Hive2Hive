package org.hive2hive.core.processes.files.move;

import java.io.IOException;
import java.security.PublicKey;
import java.util.UUID;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is pushed in a user profile queue of a user (A) when another user (B) has moved a file. The file has been
 * moved such a way that user A still has access to the file (probably the file has been moved within the same
 * shared folder or the file has been renamed).
 * 
 * @author Nico
 * 
 */
public class MoveUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = 2182278170922295626L;

	private static final Logger logger = LoggerFactory.getLogger(MoveUserProfileTask.class);

	private final String sourceFileName;
	private final String destFileName;
	private final PublicKey oldParentKey;
	private final PublicKey newParentKey;

	public MoveUserProfileTask(String sender, String sourceFileName, String destFileName, PublicKey oldParentKey,
			PublicKey newParentKey) {
		super(sender);
		this.sourceFileName = sourceFileName;
		this.destFileName = destFileName;
		this.oldParentKey = oldParentKey;
		this.newParentKey = newParentKey;
	}

	@Override
	public void start() {
		try {
			H2HSession session = networkManager.getSession();
			String randomPID = UUID.randomUUID().toString();
			UserProfileManager profileManager = session.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(randomPID, true);

			// get and check the file nodes to be rearranged
			FolderIndex oldParent = (FolderIndex) userProfile.getFileById(oldParentKey);
			if (oldParent == null) {
				logger.error("Could not find the old parent.");
				return;
			} else if (!oldParent.canWrite(sender)) {
				logger.error("User was not allowed to change the source directory.");
				return;
			}

			Index child = oldParent.getChildByName(sourceFileName);
			if (child == null) {
				logger.error("File node that should be moved not found.");
				return;
			}

			FolderIndex newParent = (FolderIndex) userProfile.getFileById(newParentKey);
			if (newParent == null) {
				logger.error("Could not find the new parent.");
				return;
			} else if (!newParent.canWrite(sender)) {
				logger.error("User was not allowed to change the destination directory.");
				return;
			}

			// rearrange
			oldParent.removeChild(child);
			newParent.addChild(child);
			child.setParent(newParent);

			// change the child's name
			child.setName(destFileName);

			profileManager.readyToPut(userProfile, randomPID);

			// move the file on disk
			FileUtil.moveFile(session.getRoot(), sourceFileName, destFileName, oldParent, newParent);
		} catch (Hive2HiveException | IOException e) {
			logger.error("Could not process the user profile task.", e);
		}
	}
}
