package org.hive2hive.core.processes.files.move;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class MoveNotificationMessageFactory extends BaseNotificationMessageFactory {

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
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new MoveNotificationMessage(receiver, sourceFileName, destFileName, oldParent, newParent);
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new MoveUserProfileTask(sender, sourceFileName, destFileName, oldParent, newParent);
	}
}
