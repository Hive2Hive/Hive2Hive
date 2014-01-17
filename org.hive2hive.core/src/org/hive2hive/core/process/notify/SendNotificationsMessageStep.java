package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.common.messages.BaseDirectMessageProcessStep;

public class SendNotificationsMessageStep extends BaseDirectMessageProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SendNotificationsMessageStep.class);
	private final Map<String, List<PeerAddress>> locations;

	public SendNotificationsMessageStep(Map<String, List<PeerAddress>> locations) {
		this.locations = locations;
	}

	@Override
	public void start() {
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		BaseNotificationMessageFactory messageFactory = context.getMessageFactory();
		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();

		for (String user : context.getUsers()) {
			PublicKey publicKey = userPublicKeys.get(user);
			if (user.equalsIgnoreCase(context.getOwnUserId())) {
				// send own peers a 'normal' notification message
				notifyMyPeers(messageFactory, publicKey);
			} else {
				// send to the master node of another client
				notifyMasterPeer(messageFactory, user, publicKey);
			}
		}

		getProcess().setNextStep(null);
	}

	private void notifyMyPeers(BaseNotificationMessageFactory messageFactory, PublicKey ownPublicKey) {
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();

		List<PeerAddress> ownPeers = locations.get(context.getOwnUserId());
		logger.debug("Notifying " + ownPeers.size() + " other peers of myself");
		for (PeerAddress peerAddress : ownPeers) {
			try {
				sendDirect(messageFactory.createPrivateNotificationMessage(peerAddress), ownPublicKey);
			} catch (SendFailedException e) {
				// add to the unreachable list, such that the next process can cleanup those
				// locations
				context.addUnreachableLocation(peerAddress);
				// continue anyhow
			}
		}
	}

	private void notifyMasterPeer(BaseNotificationMessageFactory messageFactory, String userId,
			PublicKey publicKey) {
		List<PeerAddress> peerList = locations.get(userId);
		boolean success = false;
		while (!success && !peerList.isEmpty()) {
			PeerAddress master = NetworkUtils.choseFirstPeerAddress(peerList);
			BaseDirectMessage msg = messageFactory.createHintNotificationMessage(master, userId);
			try {
				sendDirect(msg, publicKey);
				success = true;
			} catch (SendFailedException e) {
				if (!peerList.isEmpty()) {
					logger.error("Master of user " + userId + " was offline. Try next in line");
					peerList.remove(0);
				}
			}
		}

		if (success == false) {
			logger.info("All clients of user " + userId + " are currently offline or unreachable");
		} else {
			logger.debug("Successfully notified the master peer of user " + userId
					+ " that he should check his UP tasks");
		}
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// no response expected
	}
}
