package org.hive2hive.core.processes.implementations.notify;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BaseDirectMessageProcessStep;
import org.hive2hive.core.processes.implementations.context.NotifyProcessContext;

public class SendNotificationsMessageStep extends BaseDirectMessageProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SendNotificationsMessageStep.class);
	private final NotifyProcessContext context;
	private final NetworkManager networkManager;

	public SendNotificationsMessageStep(NotifyProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getMessageManager());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		BaseNotificationMessageFactory messageFactory = context.consumeMessageFactory();
		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();
		Map<String, List<PeerAddress>> locations = context.getAllLocations();

		for (String user : context.consumeUsersToNotify()) {
			PublicKey publicKey = userPublicKeys.get(user);
			List<PeerAddress> peerAddresses = locations.get(user);
			if (user.equalsIgnoreCase(networkManager.getUserId())) {
				// send own peers a 'normal' notification message
				notifyMyPeers(peerAddresses, messageFactory, publicKey);
			} else {
				// send to the initial node of another client
				notifyInitialPeer(peerAddresses, messageFactory, user, publicKey);
			}
		}
	}

	private void notifyMyPeers(List<PeerAddress> ownPeers, BaseNotificationMessageFactory messageFactory,
			PublicKey ownPublicKey) {
		ownPeers.remove(networkManager.getConnection().getPeer().getPeerAddress());
		logger.debug("Notifying " + ownPeers.size() + " other peers of me (without myself)");
		for (PeerAddress peerAddress : ownPeers) {
			if (peerAddress.equals(networkManager.getConnection().getPeer().getPeerAddress())) {
				// don't send myself
				logger.trace("Skipping to send a message to myself");
				continue;
			}

			try {
				BaseDirectMessage message = messageFactory.createPrivateNotificationMessage(peerAddress);
				if (message == null) {
					logger.info("Not notifying any of the own peers because the message to be sent is null");
				} else {
					sendDirect(message, ownPublicKey);
				}
			} catch (SendFailedException e) {
				// add to the unreachable list, such that the next process can cleanup those
				// locations
				context.addUnreachableLocation(peerAddress);
				// continue anyhow
			}
		}
	}

	private void notifyInitialPeer(List<PeerAddress> peerList, BaseNotificationMessageFactory messageFactory,
			String userId, PublicKey publicKey) {
		boolean success = false;
		while (!success && !peerList.isEmpty()) {
			PeerAddress initial = NetworkUtils.choseFirstPeerAddress(peerList);
			BaseDirectMessage msg = messageFactory.createHintNotificationMessage(initial, userId);
			try {
				sendDirect(msg, publicKey);
				success = true;
			} catch (SendFailedException e) {
				if (!peerList.isEmpty()) {
					logger.error("Initial peer of user " + userId + " was offline. Try next in line.");
					peerList.remove(0);
				}
			}
		}

		if (success == false) {
			logger.info("All clients of user " + userId + " are currently offline or unreachable.");
		} else {
			logger.debug("Successfully notified the initial peer of user " + userId
					+ " that it should check its UP tasks.");
		}
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// no response expected
	}
}
