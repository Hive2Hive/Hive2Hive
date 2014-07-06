package org.hive2hive.core.processes.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.NotifyProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets all locations of a given list of users (iterative). If all locations are fetched, this step sends
 * them.
 * 
 * @author Nico
 * 
 */
// TODO: do parallel for faster processing
public class GetAllLocationsStep extends BaseGetProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(GetAllLocationsStep.class);
	private final NotifyProcessContext context;

	public GetAllLocationsStep(NotifyProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Starting to get all locations from the users to be notified.");
		Map<String, List<PeerAddress>> allLocations = new HashMap<String, List<PeerAddress>>();

		// iterate over all users and get the locations of them
		for (String userId : context.consumeUsersToNotify()) {
			NetworkContent content = get(userId, H2HConstants.USER_LOCATIONS);
			if (content == null) {
				allLocations.put(userId, new ArrayList<PeerAddress>());
			} else {
				Locations currentLoc = (Locations) content;
				List<PeerAddress> addresses = new ArrayList<PeerAddress>(currentLoc.getPeerAddresses());
				allLocations.put(userId, addresses);
			}
		}

		// done with all locations
		logger.debug("Sending notifications to {} users.", allLocations.size());
		context.setAllLocations(allLocations);
	}
}
