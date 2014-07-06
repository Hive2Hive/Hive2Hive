package org.hive2hive.core.processes.share;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class ShareFolderNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final FolderIndex fileNode;
	private final UserPermission addedFriend;

	public ShareFolderNotificationMessageFactory(FolderIndex fileNode, UserPermission userPermission) {
		this.fileNode = fileNode;
		this.addedFriend = userPermission;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		// own clients must not be notified
		return null;
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new ShareFolderUserProfileTask(sender, fileNode, addedFriend);
	}
}
