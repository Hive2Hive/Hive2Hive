package org.hive2hive.core.process.notify.cleanup;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.process.common.put.PutLocationStep;

/**
 * Removes all locations that are unreachable and puts the reduced locations back into the DHT
 * 
 * @author Nico
 * 
 */
public class RemoveUnreachableStep extends PutLocationStep {

	private Set<PeerAddress> unreachablePeers;

	public RemoveUnreachableStep(Set<PeerAddress> unreachablePeers) {
		super(null, null);
		this.unreachablePeers = unreachablePeers;
	}

	@Override
	public void start() {
		CleanupLocationsProcessContext context = (CleanupLocationsProcessContext) getProcess().getContext();
		locations = context.getLocations();

		for (PeerAddress toRemove : unreachablePeers) {
			locations.removePeerAddress(toRemove);
		}

		super.start();
	}

}
