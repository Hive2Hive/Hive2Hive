package org.hive2hive.core.events.framework.interfaces.user;

import net.tomp2p.peers.PeerAddress;

public interface IUserLogoutEvent extends IUserEvent {

	/**
	 * @return the address of the logged out client
	 */
	PeerAddress getClientAddress();
}
