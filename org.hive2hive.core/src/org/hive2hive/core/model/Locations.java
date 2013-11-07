package org.hive2hive.core.model;

import java.util.HashSet;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * A list of unique addresses of peers that are currently online. If there is at least one client online,
 * exactly one client becomes the master. Holding these addresses is necessary to locate clients.
 * 
 * @author Nico
 * 
 */
public class Locations extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final String forUser;
	private final Set<LocationsEntry> locationsEntry;

	public Locations(String forUser) {
		this.forUser = forUser;
		locationsEntry = new HashSet<LocationsEntry>();
	}

	public String getUserId() {
		return forUser;
	}

	public void addEntry(LocationsEntry entry) {
		locationsEntry.add(entry);
	}

	public void removeEntry(LocationsEntry toRemove) {
		locationsEntry.remove(toRemove);
	}

	public void removeEntry(PeerAddress toRemove) {
		LocationsEntry removal = null;
		for (LocationsEntry online : locationsEntry) {
			if (online.getAddress().equals(toRemove)) {
				removal = online;
				break;
			}
		}
		locationsEntry.remove(removal);
	}

	public Set<LocationsEntry> getLocationsEntries() {
		return locationsEntry;
	}

	public LocationsEntry getMaster() {
		for (LocationsEntry peer : locationsEntry) {
			if (peer.isMaster())
				return peer;
		}
		return null;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getLocations();
	}
}
