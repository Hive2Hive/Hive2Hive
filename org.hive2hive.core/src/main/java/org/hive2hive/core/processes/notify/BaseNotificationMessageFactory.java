package org.hive2hive.core.processes.notify;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;

/**
 * Abstract class needed by the notification process to generate notification messages. It generates
 * notification messages for the own user, UserProfileTasks for other users and a notification message to
 * tickle one client of the other users to process their UP tasks.
 * 
 * @author Nico
 * 
 */
public abstract class BaseNotificationMessageFactory {

	/**
	 * Create a private message to notify clients of the same user.
	 * 
	 * @param receiver
	 * @return
	 */
	public abstract BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver);

	/**
	 * Create a user profile task to put it into the queue of other users.
	 * 
	 * @return
	 */
	public abstract UserProfileTask createUserProfileTask(String sender);

	/**
	 * After putting the {@link UserProfileTask} in the queue of the other users, notify them with this
	 * message
	 * 
	 * @param receiver
	 * @param userId
	 * @return
	 */
	public BaseDirectMessage createHintNotificationMessage(PeerAddress receiver, String userId) {
		return new UserProfileTaskNotificationMessage(receiver, userId);
	}
}
