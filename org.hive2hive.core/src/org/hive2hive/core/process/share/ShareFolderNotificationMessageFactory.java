package org.hive2hive.core.process.share;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class ShareFolderNotificationMessageFactory implements INotificationMessageFactory {

	private final FileTreeNode fileTreeNode;
	
	public ShareFolderNotificationMessageFactory(FileTreeNode fileTreeNode){
		this.fileTreeNode = fileTreeNode;
	}
	
	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new ShareFolderNotificationMessage(receiver, fileTreeNode);
	}

}
