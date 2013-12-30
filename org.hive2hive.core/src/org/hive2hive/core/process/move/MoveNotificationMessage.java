package org.hive2hive.core.process.move;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.ProcessManager;

/**
 * This message is sent after a file has been moved and the receiver had access to the file before and after
 * movement.
 * 
 * @author Nico
 * 
 */
public class MoveNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 2855700202146422905L;
	private final static Logger logger = H2HLoggerFactory.getLogger(MoveNotificationMessage.class);
	private final String fileName;
	private final PublicKey oldParentKey;
	private final PublicKey newParentKey;

	public MoveNotificationMessage(PeerAddress targetAddress, String fileName, PublicKey oldParentKey,
			PublicKey newParentKey) {
		super(targetAddress);
		this.fileName = fileName;
		this.oldParentKey = oldParentKey;
		this.newParentKey = newParentKey;
	}

	@Override
	public void run() {
		logger.debug("Notification message received");
		move();
	}

	private void move() {
		try {
			H2HSession session = networkManager.getSession();
			UserProfileManager profileManager = session.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(ProcessManager.createRandomPseudoPID(),
					false);

			// find the file of this user on the disc
			FileTreeNode oldParent = userProfile.getFileById(oldParentKey);
			File oldParentFile = session.getFileManager().getPath(oldParent).toFile();
			File toMoveSource = new File(oldParentFile, fileName);

			if (!toMoveSource.exists()) {
				throw new FileNotFoundException("Cannot move file '" + toMoveSource.getAbsolutePath()
						+ "' because it's not at the source location anymore");
			}

			FileTreeNode newParent = userProfile.getFileById(newParentKey);
			File newParentFile = session.getFileManager().getPath(newParent).toFile();
			File toMoveDest = new File(newParentFile, fileName);

			if (toMoveDest.exists()) {
				logger.warn("Overwriting '" + toMoveDest.getAbsolutePath()
						+ "' because file has been moved remotely");
			}

			// move the file
			Files.move(toMoveSource.toPath(), toMoveDest.toPath(), StandardCopyOption.ATOMIC_MOVE);
			logger.debug("Successfully moved the file");
		} catch (NoSessionException | GetFailedException | IOException e) {
			logger.error("Could not process the notification message", e);
		}

	}

	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		if (!networkManager.getUserId().equals(userId)) {
			logger.error("Signature is not from the same user.");
			return false;
		} else {
			return verify(data, signature, networkManager.getPublicKey());
		}
	}
}
