package org.hive2hive.core.process.upload.newfile;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.common.messages.FileNotificationMessage;
import org.hive2hive.core.process.common.messages.FileNotificationMessage.FileOperation;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class AddNotifyMessageFactory implements INotificationMessageFactory {

	private final PublicKey fileKey;

	/**
	 * @param fileKey the file that has been added
	 */
	public AddNotifyMessageFactory(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new FileNotificationMessage(receiver, FileOperation.ADDED, fileKey);
	}
}
