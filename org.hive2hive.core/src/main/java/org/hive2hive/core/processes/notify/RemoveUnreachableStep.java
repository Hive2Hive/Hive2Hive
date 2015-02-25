package org.hive2hive.core.processes.notify;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.core.processes.common.base.BaseModifyLocationsStep;

/**
 * Removes all locations that are unreachable and puts the reduced locations back into the DHT
 * 
 * @author Nico, Seppi
 */
public class RemoveUnreachableStep extends BaseModifyLocationsStep {

	private final Set<PeerAddress> unreachablePeers;

	public RemoveUnreachableStep(Set<PeerAddress> unreachablePeers, VersionManager<Locations> locationsManager,
			PublicKeyManager keyManager) {
		super(locationsManager, keyManager);
		this.unreachablePeers = unreachablePeers;
		this.setName(getClass().getName());
	}

	@Override
	protected void modify(Locations locations) {
		// remove all unreachable peers
		for (PeerAddress toRemove : unreachablePeers) {
			locations.removePeerAddress(toRemove);
		}

	}

	@Override
	protected void rollback(Locations locations) {
		// re-add all unreachable peers
		for (PeerAddress toReAdd : unreachablePeers) {
			locations.addPeerAddress(toReAdd);
		}

	}
}
