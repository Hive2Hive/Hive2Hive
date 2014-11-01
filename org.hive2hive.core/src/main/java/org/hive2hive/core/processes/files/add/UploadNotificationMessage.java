package org.hive2hive.core.processes.files.add;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent after an upload has finished.
 * 
 * @author Nico, Seppi
 */
public class UploadNotificationMessage extends BaseDirectMessage implements IFileEventGenerator {

	private static final long serialVersionUID = -695268345354561544L;

	private static final Logger logger = LoggerFactory.getLogger(UploadNotificationMessage.class);

	private final PublicKey addedFileKey;

	public UploadNotificationMessage(PeerAddress targetAddress, PublicKey addedFileKey) {
		super(targetAddress);
		this.addedFileKey = addedFileKey;
	}

	@Override
	public void run() {
		logger.debug("Upload file notification message received.");

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

		Index addedFileIndex = userProfile.getFileById(addedFileKey);

		// trigger event
		getEventBus().publish(new FileAddEvent(addedFileIndex.asFile(session.getRootFile()), addedFileIndex.isFile()));

		// if (index.isFile()) {
		// downloadSingle();
		// } else {
		// FolderIndex folder = (FolderIndex) index;
		// if (folder.getChildren().isEmpty()) {
		// downloadSingle();
		// } else {
		// downloadTree(folder);
		// }
		// }
	}
	// private void downloadSingle() {
	// try {
	// logger.debug("Got notified and start to download the file '{}'.", index.getName());
	// ProcessComponent process =
	// ProcessFactory.instance().createDownloadFileProcess(index.getFilePublicKey(),
	// networkManager);
	// process.start();
	// } catch (Exception e) {
	// logger.error("Got notified but cannot download the file.", e);
	// }
	// }
	//
	// private void downloadTree(FolderIndex folder) {
	// List<Index> files = Index.getIndexList(folder);
	// try {
	// ProcessComponent process = FileRecursionUtil.buildDownloadProcess(files, networkManager);
	// process.start();
	// logger.debug("Got notified and start downloading a file tree.");
	// } catch (Exception e) {
	// logger.error("Could not download the full tree.", e);
	// }
	// }

}
