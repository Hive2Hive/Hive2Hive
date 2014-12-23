package org.hive2hive.core.processes.files.update;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

/**
 * The notification message factory is used when a file has been updated.
 * 
 * @author Nico, Seppi
 */
public class UpdateNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final FileIndex updatedFileIndex;

	/**
	 * @param updatedFileIndex the index that has been updated (may contain sub-files)
	 */
	public UpdateNotificationMessageFactory(FileIndex updatedFileIndex) {
		this.updatedFileIndex = updatedFileIndex;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new UpdateNotificationMessage(receiver, updatedFileIndex.getFilePublicKey());
	}

	@Override
	public UserProfileTask createUserProfileTask(String sender) {
		return new UpdateUserProfileTask(sender, updatedFileIndex.getFilePublicKey(), updatedFileIndex.getMD5());
	}
}
