package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * Gets all locations of a given list of users (iterative)
 * 
 * @author Nico
 * 
 */
// TODO: do parallel for faster processing
public class GetAllLocationsStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetAllLocationsStep.class);

	private final List<String> moreToGet;
	private final Map<String, List<PeerAddress>> allLocations;
	private String currentUser;

	public GetAllLocationsStep(Set<String> userIds) {
		this(new ArrayList<String>(userIds), new HashMap<String, List<PeerAddress>>());
	}

	private GetAllLocationsStep(List<String> moreToGet, Map<String, List<PeerAddress>> allLocations) {
		this.moreToGet = moreToGet;
		this.allLocations = allLocations;
	}

	@Override
	public void start() {
		if (moreToGet.isEmpty()) {
			// notify all other clients of all users
			logger.debug("Sending notifications to " + allLocations.size() + " users");
			NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
			Map<String, BaseDirectMessage> messages = context.getNotificationMessages();
			Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();

			for (String user : allLocations.keySet()) {
				List<PeerAddress> peerList = allLocations.get(user);
				logger.debug("Notifying " + peerList.size() + " clients of user '" + user + "'.");
				for (PeerAddress peerAddress : peerList) {
					// TODO: where to get the messages from?
					getNetworkManager().sendDirect(messages.get(user), userPublicKeys.get(user), null);
				}
			}

			// done
			getProcess().setNextStep(null);
		} else {
			// get next in the list
			currentUser = moreToGet.remove(0);
			get(currentUser, H2HConstants.USER_LOCATIONS);
		}
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			allLocations.put(currentUser, new ArrayList<PeerAddress>());
		} else {
			Locations currentLoc = (Locations) content;
			List<PeerAddress> addresses = new ArrayList<PeerAddress>(currentLoc.getPeerAddresses());
			allLocations.put(currentUser, addresses);
		}

		getProcess().setNextStep(new GetAllLocationsStep(moreToGet, allLocations));
	}
}
