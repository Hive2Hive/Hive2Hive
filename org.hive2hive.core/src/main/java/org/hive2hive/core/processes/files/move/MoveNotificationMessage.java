package org.hive2hive.core.processes.files.move;

import java.io.IOException;
import java.security.PublicKey;
import java.util.UUID;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent after a file has been moved and the receiver had access to the file before and after
 * movement.
 * 
 * @author Nico
 * 
 */
public class MoveNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 2855700202146422905L;

	private static final Logger logger = LoggerFactory.getLogger(MoveNotificationMessage.class);
	private final String sourceFileName;
	private final String destFileName;
	private final PublicKey oldParentKey;
	private final PublicKey newParentKey;

	public MoveNotificationMessage(PeerAddress targetAddress, String sourceFileName, String destFileName,
			PublicKey oldParentKey, PublicKey newParentKey) {
		super(targetAddress);
		this.sourceFileName = sourceFileName;
		this.destFileName = destFileName;
		this.oldParentKey = oldParentKey;
		this.newParentKey = newParentKey;
	}

	@Override
	public void run() {
		logger.debug("Notification message received.");
		move();
	}

	private void move() {
		try {
			H2HSession session = networkManager.getSession();
			UserProfileManager profileManager = session.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(UUID.randomUUID().toString(), false);

			Index oldParent = userProfile.getFileById(oldParentKey);
			Index newParent = userProfile.getFileById(newParentKey);
			FileUtil.moveFile(session.getRoot(), sourceFileName, destFileName, oldParent, newParent);
		} catch (NoSessionException | GetFailedException | IOException e) {
			logger.error("Could not process the notification message.", e);
		}

	}

}
