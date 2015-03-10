package org.hive2hive.core.events.implementations;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.IUserLoginEvent;

public class UserLoginEvent extends UserEvent implements IUserLoginEvent {

	private final PeerAddress newClient;

	public UserLoginEvent(String currentUser, PeerAddress newClient) {
		super(currentUser);
		this.newClient = newClient;
	}

	@Override
	public PeerAddress getClientAddress() {
		return newClient;
	}

}
