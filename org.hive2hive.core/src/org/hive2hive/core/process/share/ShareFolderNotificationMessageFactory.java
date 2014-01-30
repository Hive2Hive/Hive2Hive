package org.hive2hive.core.process.share;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.process.userprofiletask.share.ShareFolderUserProfileTask;

public class ShareFolderNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final FileTreeNode fileNode;

	public ShareFolderNotificationMessageFactory(FileTreeNode fileNode) {
		this.fileNode = fileNode;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		// own clients must not be notified
		return null;
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		return new ShareFolderUserProfileTask(fileNode);
	}
}
