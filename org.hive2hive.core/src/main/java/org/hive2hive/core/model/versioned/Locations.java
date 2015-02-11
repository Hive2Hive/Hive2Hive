package org.hive2hive.core.model.versioned;

import java.util.HashSet;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.TimeToLiveStore;

/**
 * A list of unique addresses of peers that are currently online. Holding these addresses is necessary to
 * locate clients.
 * 
 * @author Nico
 * 
 */
public class Locations extends BaseVersionedNetworkContent {

	private static final long serialVersionUID = 3538720256521250650L;

	private final String userId;
	private final Set<PeerAddress> addresses;

	public Locations(String userId) {
		this.userId = userId;
		this.addresses = new HashSet<PeerAddress>();
	}

	public String getUserId() {
		return userId;
	}

	public void addPeerAddress(PeerAddress address) {
		addresses.add(address);
	}

	public void removePeerAddress(PeerAddress toRemove) {
		addresses.remove(toRemove);
	}

	public Set<PeerAddress> getPeerAddresses() {
		return addresses;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getLocations();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Locations [");
		sb.append("userId=").append(userId);
		sb.append(", ").append("addresses=[");
		for (PeerAddress peerAddress : addresses) {
			sb.append(peerAddress).append(", ");
		}
		return sb.append("]]").toString();
	}
}
