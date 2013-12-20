package org.hive2hive.core.process.upload;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.download.DownloadFileProcess;

/**
 * This message is sent after an upload has finished. It downloads the newest version at the receiver side *
 * 
 * @author Nico
 * 
 */
public class UploadNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = -695268345354561544L;
	private final static Logger logger = H2HLoggerFactory.getLogger(UploadNotificationMessage.class);
	private final PublicKey fileKey;

	public UploadNotificationMessage(PeerAddress targetAddress, PublicKey fileKey) {
		super(targetAddress);
		this.fileKey = fileKey;
	}

	@Override
	public void run() {
		logger.debug("Notification message received");
		download();
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
	
	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		if (!networkManager.getUserId().equals(userId)) {
			logger.error("Signature is not from the same user.");
			return false;
		} else {
			return verify(data, signature, networkManager.getPublicKey());
		}
	}
}
