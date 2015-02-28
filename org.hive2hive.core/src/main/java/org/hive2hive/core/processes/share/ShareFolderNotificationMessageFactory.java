package org.hive2hive.core.processes.share;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.IH2HEncryption;

public class ShareFolderNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final FolderIndex fileNode;
	private final UserPermission addedSharer;

	public ShareFolderNotificationMessageFactory(IH2HEncryption encryption, FolderIndex fileNode, UserPermission addedSharer) {
		super(encryption);
		this.fileNode = fileNode;
		this.addedSharer = addedSharer;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		// own clients must not be notified
		return null;
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new ShareFolderUserProfileTask(sender, generateProtectionKeys(), fileNode, addedSharer);
	}
}
