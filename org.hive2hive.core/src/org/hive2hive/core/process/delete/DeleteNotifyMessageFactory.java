package org.hive2hive.core.process.delete;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.common.messages.FileNotificationMessage;
import org.hive2hive.core.process.common.messages.FileNotificationMessage.FileOperation;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class DeleteNotifyMessageFactory implements INotificationMessageFactory {

	private final PublicKey fileKey;

	/**
	 * @param fileKey the file that has been deleted
	 */
	public DeleteNotifyMessageFactory(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new FileNotificationMessage(receiver, FileOperation.DELETED, fileKey);
	}
}
