package org.hive2hive.core.process.move;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class MoveNotificationMessageFactory implements INotificationMessageFactory {

	private final PublicKey fileKey;

	/**
	 * @param fileKey of the file that has been moved
	 */
	public MoveNotificationMessageFactory(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new MoveNotificationMessage(receiver, fileKey);
	}
}
