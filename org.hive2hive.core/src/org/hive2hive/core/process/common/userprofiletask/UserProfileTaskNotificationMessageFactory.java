package org.hive2hive.core.process.common.userprofiletask;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

/**
 * A general notification message factory to send user profile task notification messages to other users. The
 * receiver should then fetch his user profile task queue and handle the stored user profile tasks. The
 * {@link Type} indicates the purpose of the notification, but causes no effect.
 * 
 * @author Seppi
 */
public class UserProfileTaskNotificationMessageFactory implements INotificationMessageFactory {

	public enum Type {
		SHARING_FOLDER
	}

	private final Type type;

	public UserProfileTaskNotificationMessageFactory(Type type) {
		this.type = type;
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		return new UserProfileTaskNotificationMessage(receiver, userId, type);
	}

}
