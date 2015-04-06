package org.hive2hive.core.events.implementations;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.IUserLogoutEvent;

public class UserLogoutEvent extends UserEvent implements IUserLogoutEvent {

	private final PeerAddress leftClient;

	public UserLogoutEvent(String currentUser, PeerAddress newClient) {
		super(currentUser);
		this.leftClient = newClient;
	}

	@Override
	public PeerAddress getClientAddress() {
		return leftClient;
	}

}
