package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.common.messages.BaseDirectMessageProcessStep;

public class SendNotificationsMessageStep extends BaseDirectMessageProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SendNotificationsMessageStep.class);

	private Map<String, List<PeerAddress>> locations;

	public SendNotificationsMessageStep(Map<String, List<PeerAddress>> locations) {
		this.locations = locations;
	}

	@Override
	public void start() {
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();
		BaseNotificationMessageFactory messageFactory = context.getMessageFactory();

		for (String user : locations.keySet()) {
			boolean toOwnUser = false;
			try {
				toOwnUser = user.equals(getNetworkManager().getSession().getCredentials().getUserId());
			} catch (NoSessionException e) {
				// ignore
			}

			List<PeerAddress> peerList = locations.get(user);
			logger.debug("Notifying " + peerList.size() + " clients of user '" + user + "'.");
			for (PeerAddress peerAddress : peerList) {
				if (peerAddress.equals(getNetworkManager().getConnection().getPeer().getPeerAddress())) {
					// don't send to own peer
					continue;
				}

				BaseDirectMessage msg = messageFactory.createPrivateNotificationMessage(peerAddress);
				try {
					sendDirect(msg, userPublicKeys.get(user));
				} catch (SendFailedException e) {
					if (toOwnUser) {
						// add to the unreachable list, such that the next process can cleanup those locations
						context.addUnreachableLocation(peerAddress);
					}
				}
			}
		}

		getProcess().setNextStep(null);
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// no response expected
	}
}
