package org.hive2hive.core.processes.files.delete;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class DeleteNotifyMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey fileKey;

	public DeleteNotifyMessageFactory(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new DeleteNotificationMessage(receiver, fileKey);
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new DeleteUserProfileTask(sender, fileKey);
	}
}
