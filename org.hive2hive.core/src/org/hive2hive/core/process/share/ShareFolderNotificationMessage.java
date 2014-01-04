package org.hive2hive.core.process.share;

import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.share.notify.ShareFolderNotificationProcessContext;
import org.hive2hive.core.process.share.notify.ShareFolderNotificationProcess;

public class ShareFolderNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 7120823390008870462L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderNotificationMessage.class);

	// used for getting access to the meta folder
	private final PublicKey folderKey;
	// used for write permissions
	private final KeyPair domainKey;

	public ShareFolderNotificationMessage(PeerAddress targetAddress, PublicKey folderKey, KeyPair domainKey) {
		super(targetAddress);
		this.folderKey = folderKey;
		this.domainKey = domainKey;
	}

	@Override
	public void run() {
		logger.debug("Sharing a folder notification message received");
		ShareFolderNotificationProcess process = new ShareFolderNotificationProcess(folderKey, networkManager);
		process.start();
		logger.debug("Got notified and start to download the shared folder");
	}

	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		// TODO verify signature from foreign user
		return true;
	}
}
