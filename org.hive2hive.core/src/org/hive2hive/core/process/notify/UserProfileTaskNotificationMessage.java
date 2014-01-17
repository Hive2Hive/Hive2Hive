package org.hive2hive.core.process.notify;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.userprofiletask.UserProfileTaskQueueProcess;

/**
 * A general user profile task notification message which encourages the receiver to check his user profile
 * task queue.
 * 
 * @author Seppi
 */
public class UserProfileTaskNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 1614762764885721467L;

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileTaskNotificationMessage.class);

	private final String senderId;

	public UserProfileTaskNotificationMessage(PeerAddress targetAddress, String senderId) {
		super(targetAddress);
		this.senderId = senderId;
	}

	@Override
	public void run() {
		logger.debug(String.format("Received an user profile task notification. from = '%s'", senderId));
		try {
			UserProfileTaskQueueProcess process = new UserProfileTaskQueueProcess(networkManager);
			process.start();
		} catch (NoSessionException e) {
			logger.error("Can't handle user profile task queue. Currently no user logged in.");
		}
	}
}
