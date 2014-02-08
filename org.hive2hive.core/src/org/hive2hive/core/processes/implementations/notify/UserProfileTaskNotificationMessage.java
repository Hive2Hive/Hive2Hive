package org.hive2hive.core.processes.implementations.notify;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

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
			ProcessComponent process = ProcessFactory.instance().createUserProfileTaskStep(networkManager);
			process.start();
		} catch (InvalidProcessStateException e) {
			logger.error("Can't handle user profile task queue. Currently no user logged in.");
		}
	}
}
