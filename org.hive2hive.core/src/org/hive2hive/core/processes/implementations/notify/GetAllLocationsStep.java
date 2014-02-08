package org.hive2hive.core.processes.implementations.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.NotifyProcessContext;

/**
 * Gets all locations of a given list of users (iterative). If all locations are fetched, this step sends
 * them.
 * 
 * @author Nico
 * 
 */
// TODO: do parallel for faster processing
public class GetAllLocationsStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetAllLocationsStep.class);
	private final NotifyProcessContext context;

	public GetAllLocationsStep(NotifyProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Starting to get all locations from the users to be notified");
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
		logger.debug("Sending notifications to " + allLocations.size() + " users");
		context.setAllLocations(allLocations);
	}
}
