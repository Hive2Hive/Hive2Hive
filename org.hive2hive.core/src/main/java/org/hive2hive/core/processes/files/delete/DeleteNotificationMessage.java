package org.hive2hive.core.processes.files.delete;

import java.io.File;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class DeleteNotificationMessage extends BaseDirectMessage implements IFileEventGenerator {

	private static final long serialVersionUID = 5518489264065301800L;

	private static final Logger logger = LoggerFactory.getLogger(DeleteNotificationMessage.class);

	private final PublicKey parentFileKey;
	private final String fileName;
	private final boolean isFile;

	public DeleteNotificationMessage(PeerAddress targetAddress, PublicKey parentFileKey, String fileName, boolean isFile) {
		super(targetAddress);
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
		this.isFile = isFile;
	}

	@Override
	public void run() {
		logger.debug("File delete notification message received.");

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.");
			return;
		}

		UserProfile userProfile;
		try {
			userProfile = session.getProfileManager().readUserProfile();
		} catch (GetFailedException e) {
			logger.error("Couldn't load the user profile.");
			return;
		}

		Index parentNode = userProfile.getFileById(parentFileKey);
		if (parentNode == null) {
			logger.error("Got notified about a file we don't know the parent of.");
			return;
		} else if (parentNode.isFile()) {
			logger.error("Received id belongs to a file but should be a folder.");
			return;
		}

		// trigger event
		File parentFile = parentNode.asFile(session.getRootFile());
		File deletedFile = new File(parentFile, fileName);
		getEventBus().publish(new FileDeleteEvent(deletedFile, isFile));
	}
}
