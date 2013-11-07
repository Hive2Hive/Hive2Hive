package org.hive2hive.core.model;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Data holder for a mapping between a peer's address and its role in the DHT (master or not). Every client
 * that is online has such an entry in its {@link Locations}.
 * 
 * @author Nico
 * 
 */
public class LocationEntry extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private PeerAddress address;
	private boolean isMaster;

	public LocationEntry(PeerAddress location, boolean isMaster) {
		this.setAddress(location);
		this.setMaster(isMaster);
	}

	public PeerAddress getAddress() {
		return address;
	}

	public void setAddress(PeerAddress address) {
		this.address = address;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getLocations();
	}
}
