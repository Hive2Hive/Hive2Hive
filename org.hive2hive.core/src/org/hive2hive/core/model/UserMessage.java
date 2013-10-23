package org.hive2hive.core.model;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.DataWrapper;

/**
 * An encrypted and signed message which is stored in the DHT. The user message
 * will be stored in a queue (see {@link UserMessageQueue}). This allows an asynchronous communication between
 * users (e.g. receiving user is currently offline)
 * 
 * @author Nico
 * 
 */
public class UserMessage extends DataWrapper {

	private static final long serialVersionUID = 1L;
	private final String id;
	private final String sender;
	private final PeerAddress origin;

	public UserMessage(String id, String sender, PeerAddress origin) {
		this.id = id;
		this.sender = sender;
		this.origin = origin;
	}

	public String getId() {
		return id;
	}

	public String getSender() {
		return sender;
	}

	public PeerAddress getOrigin() {
		return origin;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserMessageQueue();
	}
}
