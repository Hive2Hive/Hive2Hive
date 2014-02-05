package org.hive2hive.core.processes.implementations.files.add;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public class UploadNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final FileTreeNode fileTreeNode;
	private final PublicKey parentKey;

	/**
	 * @param fileTreeNode the file that has been added
	 * @param parentKey the new parent's public key
	 */
	public UploadNotificationMessageFactory(FileTreeNode fileTreeNode, PublicKey parentKey) {
		this.fileTreeNode = fileTreeNode;
		this.parentKey = parentKey;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new UploadNotificationMessage(receiver, fileTreeNode.getFileKey());
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		return new UploadUserProfileTask(fileTreeNode, parentKey);
	}
}
