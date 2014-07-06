package org.hive2hive.core.processes.files.delete;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class DeleteNotifyMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey fileKey;
	private final PublicKey parentFileKey;
	private final String fileName;

	/**
	 * @param parentFileKey the file that has been deleted
	 * @param fileName to uniquely identify the deleted file
	 */
	public DeleteNotifyMessageFactory(PublicKey fileKey, PublicKey parentFileKey, String fileName) {
		this.fileKey = fileKey;
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new DeleteNotificationMessage(receiver, parentFileKey, fileName);
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new DeleteUserProfileTask(sender, fileKey);
	}
}
