package org.hive2hive.core.process.delete;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Random;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * Performs the necessary processes when another user did any modification on the file
 * 
 * @author Nico
 * 
 */
public class DeleteNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = -695268345354561544L;
	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteNotificationMessage.class);
	private final PublicKey parentFileKey;
	private final String fileName;

	public DeleteNotificationMessage(PeerAddress targetAddress, PublicKey parentFileKey, String fileName) {
		super(targetAddress);
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
	}

	@Override
	public void run() {
		logger.debug("File notification message received");
		delete();
	}

	private void delete() {
		try {
			// TODO file key does not exist anymore
			H2HSession session = networkManager.getSession();

			// create simulated PID
			int simulatedPID = new Random().nextInt(100) * -1;
			FileManager fileManager = session.getFileManager();
			UserProfile userProfile = session.getProfileManager().getUserProfile(simulatedPID, false);
			FileTreeNode parentNode = userProfile.getFileById(parentFileKey);

			if (parentNode == null) {
				throw new FileNotFoundException("Got notified about a file we don't know the parent");
			} else {
				boolean deleted = new File(fileManager.getFile(parentNode), fileName).delete();
				if (!deleted) {
					throw new IOException("Could not delete the file");
				}
			}
		} catch (Exception e) {
			logger.error("Got notified but cannot delete the file", e);
		}
	}
}
