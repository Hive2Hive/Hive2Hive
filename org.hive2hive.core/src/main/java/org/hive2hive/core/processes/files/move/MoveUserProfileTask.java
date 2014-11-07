package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.security.PublicKey;
import java.util.Random;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileMoveEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class MoveUserProfileTask extends UserProfileTask implements IFileEventGenerator {

	private static final long serialVersionUID = 2182278170922295626L;

	private static final Logger logger = LoggerFactory.getLogger(MoveUserProfileTask.class);

	private final String sourceFileName;
	private final String destFileName;
	private final PublicKey oldParentKey;
	private final PublicKey newParentKey;

	private final int forkLimit = 2;

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
		Index movedNode = null;
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
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

			movedNode = oldParentNode.getChildByName(sourceFileName);
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
				if (forkCounter++ > forkLimit) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;

					// retry update of user profile
					continue;
				}
			} catch (PutFailedException e) {
				logger.error("Couldn't put updated user profile.", e);
				return;
			}
			break;
		}

		try {
			// notify own other clients
			notifyOtherClients(new MoveNotificationMessageFactory(sourceFileName, destFileName, oldParentKey, newParentKey));
			logger.debug("Notified other clients that a file has been moved by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the moved file.", e);
		}

		// trigger event
		File srcParent = oldParentNode.asFile(session.getRootFile());
		File src = new File(srcParent, sourceFileName);
		File dstParent = newParentNode.asFile(session.getRootFile());
		File dst = new File(dstParent, destFileName);
		networkManager.getEventBus().publish(new FileMoveEvent(src, dst, movedNode.isFile()));
	}

}
