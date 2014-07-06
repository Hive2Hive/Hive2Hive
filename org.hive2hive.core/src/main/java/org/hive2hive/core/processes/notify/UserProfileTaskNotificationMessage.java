package org.hive2hive.core.processes.notify;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A general user profile task notification message which encourages the receiver to check his user profile
 * task queue.
 * 
 * @author Seppi
 */
public class UserProfileTaskNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 1614762764885721467L;
	private static final Logger logger = LoggerFactory.getLogger(UserProfileTaskNotificationMessage.class);

	private final String senderId;

	public UserProfileTaskNotificationMessage(PeerAddress targetAddress, String senderId) {
		super(targetAddress);
		this.senderId = senderId;
	}

	@Override
	public void run() {
		logger.debug("Received a user profile task notification from '{}'.", senderId);
		try {
			ProcessComponent process = ProcessFactory.instance().createUserProfileTaskStep(networkManager);
			process.start();
		} catch (InvalidProcessStateException e) {
			logger.error("Cannot handle user profile task queue. Currently no user is logged in.");
		}
	}
}
