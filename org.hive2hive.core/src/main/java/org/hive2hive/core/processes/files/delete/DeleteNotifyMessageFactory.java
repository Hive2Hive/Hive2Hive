package org.hive2hive.core.processes.files.delete;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.IH2HEncryption;

public class DeleteNotifyMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey fileKey;
	private final PublicKey parentFileKey;
	private final String fileName;
	private final boolean isFile;

	public DeleteNotifyMessageFactory(IH2HEncryption encryption, PublicKey fileKey, PublicKey parentFileKey,
			String fileName, boolean isFile) {
		super(encryption);
		this.fileKey = fileKey;
		this.parentFileKey = parentFileKey;
		this.fileName = fileName;
		this.isFile = isFile;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new DeleteNotificationMessage(receiver, parentFileKey, fileName, isFile);
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new DeleteUserProfileTask(sender, generateProtectionKeys(), fileKey);
	}

}
