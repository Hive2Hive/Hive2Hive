package org.hive2hive.core.process.common.messages;

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
import org.hive2hive.core.process.download.DownloadFileProcess;

/**
 * Performs the necessary processes when another user did any modification on the file
 * 
 * @author Nico
 * 
 */
public class FileNotificationMessage extends BaseDirectMessage {

	public enum FileOperation {
		ADDED,
		MODIFIED,
		DELETED;
	}

	private static final long serialVersionUID = -695268345354561544L;
	private final static Logger logger = H2HLoggerFactory.getLogger(FileNotificationMessage.class);
	private final FileOperation operation;
	private final PublicKey fileKey;

	public FileNotificationMessage(PeerAddress targetAddress, FileOperation operation, PublicKey fileKey) {
		super(targetAddress);
		this.operation = operation;
		this.fileKey = fileKey;
	}

	@Override
	public void run() {
		logger.debug("File notification message received. Operation: " + operation);
		switch (operation) {
			case ADDED:
			case MODIFIED:
				download();
				break;
			case DELETED:
				delete();
			default:
				break;
		}
	}

	private void download() {
		try {
			DownloadFileProcess process = new DownloadFileProcess(fileKey, networkManager);
			process.start();
			logger.debug("Got notified and start to download the file");
		} catch (Exception e) {
			logger.error("Got notified but cannot download the file", e);
		}
	}

	private void delete() {
		try {
			H2HSession session = networkManager.getSession();

			// create simulated PID
			int simulatedPID = new Random().nextInt(100) * -1;
			FileManager fileManager = session.getFileManager();
			UserProfile userProfile = session.getProfileManager().getUserProfile(simulatedPID, false);
			FileTreeNode fileNode = userProfile.getFileById(fileKey);

			if (fileNode == null) {
				throw new FileNotFoundException("Got notified about a file we don't know");
			} else {
				boolean deleted = fileManager.getFile(fileNode).delete();
				if (!deleted) {
					throw new IOException("Could not delete the file");
				}
			}
		} catch (Exception e) {
			logger.error("Got notified but cannot delete the file", e);
		}
	}
}
