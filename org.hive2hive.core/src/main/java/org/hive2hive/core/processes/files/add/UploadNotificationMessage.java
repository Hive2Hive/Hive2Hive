package org.hive2hive.core.processes.files.add;

import java.util.List;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.util.FileRecursionUtil;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent after an upload has finished. It downloads the newest version at the receiver side
 * 
 * @author Nico
 * 
 */
public class UploadNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = -695268345354561544L;
	private static final Logger logger = LoggerFactory.getLogger(UploadNotificationMessage.class);

	private final Index index;

	public UploadNotificationMessage(PeerAddress targetAddress, Index index) {
		super(targetAddress);
		this.index = index;
	}

	@Override
	public void run() {
		logger.debug("Notification message received.");
		if (index.isFile()) {
			downloadSingle();
		} else {
			FolderIndex folder = (FolderIndex) index;
			if (folder.getChildren().isEmpty()) {
				downloadSingle();
			} else {
				downloadTree(folder);
			}
		}
	}

	private void downloadSingle() {
		try {
			logger.debug("Got notified and start to download the file '{}'.", index.getName());
			ProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(index.getFilePublicKey(),
					networkManager);
			process.start();
		} catch (Exception e) {
			logger.error("Got notified but cannot download the file.", e);
		}
	}

	private void downloadTree(FolderIndex folder) {
		List<Index> files = Index.getIndexList(folder);
		try {
			ProcessComponent process = FileRecursionUtil.buildDownloadProcess(files, networkManager);
			process.start();
			logger.debug("Got notified and start downloading a file tree.");
		} catch (Exception e) {
			logger.error("Could not download the full tree.", e);
		}
	}
}
