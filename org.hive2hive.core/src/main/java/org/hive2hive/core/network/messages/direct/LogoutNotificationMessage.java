package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.events.framework.interfaces.IUserEventGenerator;
import org.hive2hive.core.events.implementations.UserLogoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small notification message to let the other peers know that this client went offline
 * 
 * @author Nico
 */
public class LogoutNotificationMessage extends BaseDirectMessage implements IUserEventGenerator {

	private static final long serialVersionUID = 3217766049915160658L;
	private static final Logger logger = LoggerFactory.getLogger(LogoutNotificationMessage.class);

	public LogoutNotificationMessage(PeerAddress targetPeerAddress) {
		super(targetPeerAddress);
	}

	@Override
	public void run() {
		// generate an event for the logged out client
		String currentUser = networkManager.getUserId();
		UserLogoutEvent event = new UserLogoutEvent(currentUser, senderAddress);
		networkManager.getEventBus().publish(event);
		logger.debug("Published logout event of user {} with gone client {}", currentUser, senderAddress);
	}

}
