package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileMoveEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent after a file has been moved and the receiver had access to the file before and after
 * movement.
 * 
 * @author Nico, Seppi
 */
public class MoveNotificationMessage extends BaseDirectMessage implements IFileEventGenerator {

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
		logger.debug("Move file notification message received.");

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.");
			return;
		}

		UserProfileManager profileManager = session.getProfileManager();

		UserProfile userProfile;
		try {
			userProfile = profileManager.getUserProfile(getMessageID(), false);
		} catch (GetFailedException e) {
			logger.error("Couldn't load user profile.", e);
			return;
		}

		Index oldParentNode = userProfile.getFileById(oldParentKey);
		if (oldParentNode == null) {
			logger.error("Got notified about a file we don't know.");
			return;
		}
		Index newParentNode = userProfile.getFileById(newParentKey);
		if (newParentNode == null) {
			logger.error("Got notified about a file we don't know.");
			return;
		}
		Index movedNode = ((FolderIndex) newParentNode).getChildByName(destFileName);
		if (movedNode == null) {
			logger.error("Got notified about a file we don't know.");
			return;
		}

		// trigger event
		File srcParent = oldParentNode.asFile(session.getRootFile());
		File src = new File(srcParent, sourceFileName);
		File dstParent = newParentNode.asFile(session.getRootFile());
		File dst = new File(dstParent, destFileName);
		getEventBus().publish(new FileMoveEvent(src, dst, movedNode.isFile()));
	}

}
