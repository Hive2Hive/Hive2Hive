package org.hive2hive.core.processes.implementations.files.add;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.IndexNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public class UploadNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final IndexNode indexNode;
	private final PublicKey parentKey;

	/**
	 * @param indexNode the file that has been added
	 * @param parentKey the new parent's public key
	 */
	public UploadNotificationMessageFactory(IndexNode indexNode, PublicKey parentKey) {
		this.indexNode = indexNode;
		this.parentKey = parentKey;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new UploadNotificationMessage(receiver, indexNode.getFileKey());
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		return new UploadUserProfileTask(indexNode, parentKey);
	}
}
