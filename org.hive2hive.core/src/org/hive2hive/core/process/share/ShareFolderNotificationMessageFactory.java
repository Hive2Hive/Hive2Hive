package org.hive2hive.core.process.share;

import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class ShareFolderNotificationMessageFactory implements INotificationMessageFactory {

	private final PublicKey folderKey;
	private final KeyPair domainKey;
	
	public ShareFolderNotificationMessageFactory(PublicKey folderKey){
		this.folderKey = folderKey;
		this.domainKey = null;
	}
	
	public ShareFolderNotificationMessageFactory(PublicKey folderKey, KeyPair domainKey){
		this.folderKey = folderKey;
		this.domainKey = domainKey;
	}
	
	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new ShareFolderNotificationMessage(receiver, folderKey, domainKey);
	}

}
