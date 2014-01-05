package org.hive2hive.core.process.share;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.share.notify.ShareFolderNotificationProcess;

public class ShareFolderNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 7120823390008870462L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderNotificationMessage.class);

	private final FileTreeNode fileTreeNode;

	public ShareFolderNotificationMessage(PeerAddress targetAddress, FileTreeNode fileTreeNode) {
		super(targetAddress);
		this.fileTreeNode = fileTreeNode;
	}

	@Override
	public void run() {
		logger.debug("Sharing a folder notification message received");
		try {
			ShareFolderNotificationProcess process = new ShareFolderNotificationProcess(fileTreeNode,
					networkManager);
			process.start();
			logger.debug("Got notified and start to download the shared folder");
		} catch (NoSessionException e) {
			logger.error("Got notified but can not download the shared folder");
		}
	}

	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		// TODO verify signature from foreign user
		return true;
	}
}
