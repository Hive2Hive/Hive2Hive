package org.hive2hive.core.process.upload;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class UploadNotificationMessageFactory implements INotificationMessageFactory {

	private final PublicKey fileKey;

	/**
	 * @param fileKey the file that has been added
	 */
	public UploadNotificationMessageFactory(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new UploadNotificationMessage(receiver, fileKey);
	}
}
