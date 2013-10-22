package org.hive2hive.core.model;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

/**
 * A list of unique addresses of peers that are currently online. If there is at least one client online,
 * exaclty one client becomes the master. Holding these addresses is necessary to locate clients.
 * 
 * @author Nico
 * 
 */
public class Locations {

	private final String forUser;
	private final List<OnlinePeer> onlinePeers;

	public Locations(String forUser) {
		this.forUser = forUser;
		onlinePeers = new ArrayList<OnlinePeer>();
	}

	public String getForUser() {
		return forUser;
	}

	/**
	 * Adds the address to the locations and checks if all other locations are online.
	 * 
	 * @param address the address to add (usually the address of the caller itself)
	 * @return true if the newly entered address is now the master (no notification)
	 */
	public boolean addOnlinePeer(PeerAddress address) {
		List<OnlinePeer> toRemove = new ArrayList<OnlinePeer>();
		boolean shouldBecomeMaster = false;
		for (OnlinePeer peer : onlinePeers) {
			if (!isOnline(peer.getAddress())) {
				// remove this one
				toRemove.add(peer);
				if (peer.isMaster()) {
					// Master peer offline --> new entry becomes master
					shouldBecomeMaster = true;
				}
			}
		}

		onlinePeers.removeAll(toRemove);
		onlinePeers.add(new OnlinePeer(address, shouldBecomeMaster));
		return shouldBecomeMaster;
	}

	private boolean isOnline(PeerAddress toCheck) {
		// TODO: Implement (Network manager is needed) or use a Util-Class for that
		return true;
	}

	public OnlinePeer getMaster() {
		for (OnlinePeer peer : onlinePeers) {
			if (peer.isMaster())
				return peer;
		}
		return null;
	}
}
