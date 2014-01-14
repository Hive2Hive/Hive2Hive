package org.hive2hive.core.process.notify.cleanup;

import java.security.KeyPair;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Removes all locations that are unreachable and puts the reduced locations back into the DHT
 * 
 * @author Nico
 * 
 */
public class RemoveUnreachableStep extends BasePutProcessStep {

	private Set<PeerAddress> unreachablePeers;

	public RemoveUnreachableStep(Set<PeerAddress> unreachablePeers) {
		super(null);
		this.unreachablePeers = unreachablePeers;
	}

	@Override
	public void start() {
		CleanupLocationsProcessContext context = (CleanupLocationsProcessContext) getProcess().getContext();
		Locations locations = context.getLocations();

		KeyPair protectionKeys;
		try {
			protectionKeys = context.getH2HSession().getProfileManager().getDefaultProtectionKey();
		} catch (GetFailedException e) {
			getProcess().stop(e);
			return;
		}

		for (PeerAddress toRemove : unreachablePeers) {
			locations.removePeerAddress(toRemove);
		}

		locations.setBasedOnKey(locations.getVersionKey());
		locations.generateVersionKey();

		put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, protectionKeys);
	}

}
