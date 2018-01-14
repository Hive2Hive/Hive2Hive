package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileMoveEvent;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.IUserProfileModification;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class MoveUserProfileTask extends UserProfileTask implements IUserProfileModification, IFileEventGenerator {

	private static final long serialVersionUID = 2182278170922295626L;

	private static final Logger logger = LoggerFactory.getLogger(MoveUserProfileTask.class);

	private final String sourceFileName;
	private final String destFileName;
	private final PublicKey oldParentKey;
	private final PublicKey newParentKey;

	// initialized during user profile modification
	private FolderIndex oldParentNode;
	private Index movedNode;
	private FolderIndex newParentNode;

	public MoveUserProfileTask(String sender, KeyPair protectionKeys, String sourceFileName, String destFileName,
			PublicKey oldParentKey, PublicKey newParentKey) {
		super(sender, protectionKeys);
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

		UserProfileManager profileManager = session.getProfileManager();

		try {
			profileManager.modifyUserProfile(getId(), this);
		} catch (Hive2HiveException e) {
			logger.error("Couldn't update / modify the user profile.", e);
			return;
		}

		try {
			// notify own other clients
			notifyOtherClients(new MoveNotificationMessageFactory(networkManager.getEncryption(), sourceFileName,
					destFileName, oldParentKey, newParentKey));
			logger.debug("Notified other clients that a file has been moved by another user.");
		} catch (IllegalArgumentException | NoPeerConnectionException | NoSessionException e) {
			logger.error("Could not notify other clients of me about the moved file.", e);
		}

		// trigger event
		File srcParent = oldParentNode.asFile(session.getRootFile());
		File src = new File(srcParent, sourceFileName);
		File dstParent = newParentNode.asFile(session.getRootFile());
		File dst = new File(dstParent, destFileName);
		networkManager.getEventBus().publish(new FileMoveEvent(src, dst, movedNode.isFile()));
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		// get and check the file nodes to be rearranged
		oldParentNode = (FolderIndex) userProfile.getFileById(oldParentKey);
		if (oldParentNode == null) {
			throw new AbortModifyException("Could not find the old parent.");
		} else if (!oldParentNode.canWrite(sender)) {
			throw new AbortModifyException("User was not allowed to change the source directory.");
		}

		movedNode = oldParentNode.getChildByName(sourceFileName);
		if (movedNode == null) {
			throw new AbortModifyException("File node that should be moved not found.");
		}

		newParentNode = (FolderIndex) userProfile.getFileById(newParentKey);
		if (newParentNode == null) {
			throw new AbortModifyException("Could not find the new parent.");
		} else if (!newParentNode.canWrite(sender)) {
			throw new AbortModifyException("User was not allowed to change the destination directory.");
		}

		// relink
		oldParentNode.removeChild(movedNode);
		newParentNode.addChild(movedNode);
		movedNode.setParent(newParentNode);

		// change the child's name
		movedNode.setName(destFileName);
	}
}
