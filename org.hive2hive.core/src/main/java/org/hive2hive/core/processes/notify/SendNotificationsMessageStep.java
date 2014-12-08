package org.hive2hive.core.processes.notify;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.common.base.BaseMessageProcessStep;
import org.hive2hive.core.processes.context.NotifyProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendNotificationsMessageStep extends BaseMessageProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(SendNotificationsMessageStep.class);
	private final NotifyProcessContext context;
	private final NetworkManager networkManager;
	private final Set<PeerAddress> unreachablePeers;

	public SendNotificationsMessageStep(NotifyProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getMessageManager());
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
		this.unreachablePeers = new HashSet<PeerAddress>();
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException {
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
				notifyMasterPeer(peerAddresses, messageFactory, user, publicKey);
			}
		}

		if (!unreachablePeers.isEmpty()) {
			logger.debug("Need to cleanup {} unreachable peers of own user", unreachablePeers.size());
			try {
				H2HSession session = networkManager.getSession();
				getParent().add(
						new RemoveUnreachableStep(unreachablePeers, session.getLocationsManager(), session.getKeyManager()));
			} catch (NoSessionException e) {
				logger.error("Cannot cleanup unreachable peers because no session");
			}
		}

		return null;
	}

	private void notifyMyPeers(List<PeerAddress> ownPeers, BaseNotificationMessageFactory messageFactory,
			PublicKey ownPublicKey) {
		ownPeers.remove(networkManager.getConnection().getPeer().peerAddress());
		logger.debug("Notifying {} other peers of me (without myself).", ownPeers.size());
		for (PeerAddress peerAddress : ownPeers) {
			if (peerAddress.equals(networkManager.getConnection().getPeer().peerAddress())) {
				// don't send myself
				logger.trace("Skipping to send a message to myself.");
				continue;
			}

			try {
				BaseDirectMessage message = messageFactory.createPrivateNotificationMessage(peerAddress);
				if (message == null) {
					logger.info("Not notifying any of the own peers because the message to be sent is null.");
				} else {
					send(message, ownPublicKey);
				}
			} catch (SendFailedException e) {
				// add to the unreachable list, such that the next step can cleanup those locations
				logger.debug("Cannot notify own peer {}. Will remove it from the locations soon.", peerAddress);
				unreachablePeers.add(peerAddress);
				// continue anyhow
			}
		}
	}

	private void notifyMasterPeer(List<PeerAddress> peerList, BaseNotificationMessageFactory messageFactory, String userId,
			PublicKey publicKey) {
		boolean success = false;
		while (!success && !peerList.isEmpty()) {
			PeerAddress initial = NetworkUtils.choseFirstPeerAddress(peerList);
			BaseDirectMessage msg = messageFactory.createHintNotificationMessage(initial, userId);
			try {
				send(msg, publicKey);
				success = true;
			} catch (SendFailedException e) {
				if (!peerList.isEmpty()) {
					logger.error("Initial peer of user '{}' was offline. Try next in line.", userId, e);
					peerList.remove(0);
				}
			}
		}

		if (success) {
			logger.debug("Successfully notified the initial peer of user '{}' that it should check its UP tasks.", userId);
		} else {
			logger.info("All clients of user '{}' are currently offline or unreachable.", userId);
		}
	}

	@Override
	public void handleResponse(ResponseMessage responseMessage) {
		// no response expected
	}
}
