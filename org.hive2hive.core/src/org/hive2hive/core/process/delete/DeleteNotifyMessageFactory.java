package org.hive2hive.core.process.delete;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class DeleteNotifyMessageFactory implements INotificationMessageFactory {

	private final PublicKey parentFileKey;
	private final String fileName;

	/**
	 * @param parentFileKey the file that has been deleted
	 */
	public DeleteNotifyMessageFactory(PublicKey parentFileKey, String fileName) {
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new DeleteNotificationMessage(receiver, parentFileKey, fileName);
	}
}
