package org.hive2hive.core.process.share;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

public class ShareFolderNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey metaFolderId;
	private final KeyPair domainKey;
	private String addedUser;

	public ShareFolderNotificationMessageFactory(PublicKey metaFolderId, KeyPair domainKey, String addedUser,
			Set<String> users) {
		super(users);
		this.metaFolderId = metaFolderId;
		this.domainKey = domainKey;
		this.addedUser = addedUser;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new ShareFolderNotificationMessage(receiver, metaFolderId, domainKey, addedUser);
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
