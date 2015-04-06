package org.hive2hive.core.events.framework.interfaces.user;

import net.tomp2p.peers.PeerAddress;

public interface IUserLoginEvent extends IUserEvent {

	/**
	 * @return the new client's address
	 */
	PeerAddress getClientAddress();
}
