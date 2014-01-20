package org.hive2hive.core.process.share;

import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.process.userprofiletask.share.ShareFolderUserProfileTask;

public class ShareFolderNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey metaFolderId;
	private final KeyPair domainKey;
	private final String addedUser;
	private final FileTreeNode fileNode;

	public ShareFolderNotificationMessageFactory(PublicKey metaFolderId, KeyPair domainKey, String addedUser,
			FileTreeNode fileNode) {
		this.metaFolderId = metaFolderId;
		this.domainKey = domainKey;
		this.addedUser = addedUser;
		this.fileNode = fileNode;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new ShareFolderNotificationMessage(receiver, metaFolderId, domainKey, addedUser);
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		return new ShareFolderUserProfileTask(fileNode);
	}
}
