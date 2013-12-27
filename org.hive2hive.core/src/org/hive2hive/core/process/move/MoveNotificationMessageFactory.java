package org.hive2hive.core.process.move;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class MoveNotificationMessageFactory implements INotificationMessageFactory {

	private final String fileName;
	private final PublicKey oldParent;
	private final PublicKey newParent;

	/**
	 * @param fileKey of the file that has been moved
	 */
	public MoveNotificationMessageFactory(String fileName, PublicKey oldParent, PublicKey newParent) {
		this.fileName = fileName;
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new MoveNotificationMessage(receiver, fileName, oldParent, newParent);
	}
}
