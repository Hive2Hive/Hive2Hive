package org.hive2hive.core.process.delete;

import java.security.PublicKey;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

public class DeleteNotifyMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey parentFileKey;
	private final String fileName;

	/**
	 * @param parentFileKey the file that has been deleted
	 */
	public DeleteNotifyMessageFactory(PublicKey parentFileKey, String fileName, Set<String> users) {
		super(users);
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new DeleteNotificationMessage(receiver, parentFileKey, fileName);
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
