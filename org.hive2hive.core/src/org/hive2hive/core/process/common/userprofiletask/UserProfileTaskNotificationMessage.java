package org.hive2hive.core.process.common.userprofiletask;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.common.userprofiletask.UserProfileTaskNotificationMessageFactory.Type;
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
	private final Type type;

	public UserProfileTaskNotificationMessage(PeerAddress targetAddress, String senderId, Type type) {
		super(targetAddress);
		this.senderId = senderId;
		this.type = type;
	}

	@Override
	public void run() {
		logger.debug(String.format("Received an user profile task notification. from = '%s' type = '%s'",
				senderId, type));
		try {
			UserProfileTaskQueueProcess process = new UserProfileTaskQueueProcess(networkManager);
			process.start();
		} catch (NoSessionException e) {
			logger.error("Can't handle user profile task queue. Currently no user logged in.");
		}
	}

	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		if (networkManager.getUserId().equals(userId)) {
			logger.error("Received an user profile task from same user.");
			return false;
		} else {
			// TODO verify message from another user (problem: getting sender's public key)
			return true;
		}
	}
}
