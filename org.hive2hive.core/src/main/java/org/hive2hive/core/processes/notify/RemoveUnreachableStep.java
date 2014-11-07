package org.hive2hive.core.processes.notify;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
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

	private final NetworkManager networkManager;
	private final Set<PeerAddress> unreachablePeers;

	public RemoveUnreachableStep(Set<PeerAddress> unreachablePeers, NetworkManager networkManager) {
		this.unreachablePeers = unreachablePeers;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile userProfile;
		try {
			userProfile = networkManager.getSession().getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		VersionManager<Locations> locationsManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

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
				locationsManager.put(locations, userProfile.getProtectionKeys());
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
