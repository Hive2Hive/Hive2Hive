package org.hive2hive.core.processes.files.move;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileMoveEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is pushed in a user profile queue of a user (A) when another user (B) has moved a file. The file has been
 * moved such a way that user A still has access to the file (probably the file has been moved within the same
 * shared folder or the file has been renamed).
 * 
 * @author Nico, Seppi
 */
public class MoveUserProfileTask extends UserProfileTask implements IFileEventGenerator {

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
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.", e);
			return;
		}

		FolderIndex newParentNode = null;
		FolderIndex oldParentNode = null;
		while (true) {
			UserProfileManager profileManager = session.getProfileManager();

			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getId(), true);
			} catch (GetFailedException e) {
				logger.error("Couldn't get user profile.", e);
				return;
			}

			// get and check the file nodes to be rearranged
			oldParentNode = (FolderIndex) userProfile.getFileById(oldParentKey);
			if (oldParentNode == null) {
				logger.error("Could not find the old parent.");
				return;
			} else if (!oldParentNode.canWrite(sender)) {
				logger.error("User was not allowed to change the source directory.");
				return;
			}

			Index movedNode = oldParentNode.getChildByName(sourceFileName);
			if (movedNode == null) {
				logger.error("File node that should be moved not found.");
				return;
			}

			newParentNode = (FolderIndex) userProfile.getFileById(newParentKey);
			if (newParentNode == null) {
				logger.error("Could not find the new parent.");
				return;
			} else if (!newParentNode.canWrite(sender)) {
				logger.error("User was not allowed to change the destination directory.");
				return;
			}

			// relink
			oldParentNode.removeChild(movedNode);
			newParentNode.addChild(movedNode);
			movedNode.setParent(newParentNode);

			// change the child's name
			movedNode.setName(destFileName);

			try {
				profileManager.readyToPut(userProfile, getId());
			} catch (VersionForkAfterPutException e) {
				continue;
			} catch (PutFailedException e) {
				logger.error("Couldn't put updated user profile.", e);
				return;
			}
			break;
		}

		// event
		Path srcParentPath = FileUtil.getPath(session.getRoot(), oldParentNode);
		Path src = Paths.get(srcParentPath.toString(), sourceFileName);
		Path dstParentPath = FileUtil.getPath(session.getRoot(), newParentNode);
		Path dst = Paths.get(dstParentPath.toString(), destFileName);
		networkManager.getEventBus().publish(new FileMoveEvent(src, dst, Files.isRegularFile(src)));

		try {
			// move the file on disk
			FileUtil.moveFile(session.getRoot(), sourceFileName, destFileName, oldParentNode, newParentNode);
		} catch (IOException e) {
			logger.error("Couldn't move file on disk.", e);
		}
	}
}
