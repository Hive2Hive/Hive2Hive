package org.hive2hive.core.processes.notify;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Removes all locations that are unreachable and puts the reduced locations back into the DHT
 * 
 * @author Nico, Seppi
 */
public class RemoveUnreachableStep extends ProcessStep<Void> {

	private final Set<PeerAddress> unreachablePeers;
	private final VersionManager<Locations> locationsManager;
	private final PublicKeyManager keyManager;

	public RemoveUnreachableStep(Set<PeerAddress> unreachablePeers, VersionManager<Locations> locationsManager,
			PublicKeyManager keyManager) {
		this.locationsManager = locationsManager;
		this.keyManager = keyManager;
		this.unreachablePeers = unreachablePeers;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// TODO remove while loop
		while (true) {
			Locations locations;
			try {
				locations = locationsManager.get();
			} catch (GetFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}

			// remove peer
			for (PeerAddress toRemove : unreachablePeers) {
				locations.removePeerAddress(toRemove);
			}

			try {
				locationsManager.put(locations, keyManager.getDefaultProtectionKeyPair());
			} catch (VersionForkAfterPutException e) {
				continue;
			} catch (PutFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}

			break;
		}

		return null;
	}
}
