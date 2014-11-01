package org.hive2hive.core.processes.files.add;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

/**
 * The notification message factory is used when a file has been added.
 * 
 * @author Nico, Seppi
 */
public class AddNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final Index addedFileIndex;
	private final PublicKey parentKey;

	/**
	 * @param addedFileKey the key of file that has been added (may contain sub-files)
	 * @param parentKey the new parent's public key
	 */
	public AddNotificationMessageFactory(Index addedFileIndex, PublicKey parentKey) {
		this.addedFileIndex = addedFileIndex;
		this.parentKey = parentKey;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new AddNotificationMessage(receiver, addedFileIndex.getFilePublicKey());
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new AddUserProfileTask(sender, addedFileIndex, parentKey);
	}
}
