package org.hive2hive.core.processes.implementations.files.delete;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.UUID;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Index;
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
			H2HSession session = networkManager.getSession();
			String pid = UUID.randomUUID().toString();

			Path root = session.getRoot();
			UserProfile userProfile = session.getProfileManager().getUserProfile(pid, false);
			Index parentNode = userProfile.getFileById(parentFileKey);

			if (parentNode == null) {
				throw new FileNotFoundException("Got notified about a file we don't know the parent");
			} else {
				boolean deleted = new File(FileUtil.getPath(root, parentNode).toFile(), fileName).delete();
				if (!deleted) {
					throw new IOException("Could not delete the file");
				}
			}
		} catch (Exception e) {
			logger.error("Got notified but cannot delete the file", e);
		}
	}

}
