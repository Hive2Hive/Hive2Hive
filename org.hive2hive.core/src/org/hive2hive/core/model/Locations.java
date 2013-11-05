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
	private final Set<OnlinePeer> onlinePeers;

	public Locations(String forUser) {
		this.forUser = forUser;
		onlinePeers = new HashSet<OnlinePeer>();
	}

	public String getUserId() {
		return forUser;
	}

	public void addOnlinePeer(OnlinePeer onlinePeer) {
		onlinePeers.add(onlinePeer);
	}

	public void removeOnlinePeer(OnlinePeer toRemove) {
		onlinePeers.remove(toRemove);
	}

	public void removeOnlinePeer(PeerAddress toRemove) {
		OnlinePeer removal = null;
		for (OnlinePeer online : onlinePeers) {
			if (online.getAddress().equals(toRemove)) {
				removal = online;
				break;
			}
		}
		onlinePeers.remove(removal);
	}

	public Set<OnlinePeer> getOnlinePeers() {
		return onlinePeers;
	}

	public OnlinePeer getMaster() {
		for (OnlinePeer peer : onlinePeers) {
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
