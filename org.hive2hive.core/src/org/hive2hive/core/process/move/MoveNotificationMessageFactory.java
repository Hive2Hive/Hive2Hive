package org.hive2hive.core.process.move;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class MoveNotificationMessageFactory implements INotificationMessageFactory {

	private final String sourceFileName;
	private final String destFileName;
	private final PublicKey oldParent;
	private final PublicKey newParent;

	/**
	 * Message factory for notification messages when a file has been moved
	 * 
	 * @param sourceFileName the original file name
	 * @param destFileName the new file name (considers renaming of the files)
	 * @param oldParent the former parent key
	 * @param newParent the key of the new parent
	 */
	public MoveNotificationMessageFactory(String sourceFileName, String destFileName, PublicKey oldParent,
			PublicKey newParent) {
		this.sourceFileName = sourceFileName;
		this.destFileName = destFileName;
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new MoveNotificationMessage(receiver, sourceFileName, destFileName, oldParent, newParent);
	}
}
