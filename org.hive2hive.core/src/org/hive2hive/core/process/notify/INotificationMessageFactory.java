package org.hive2hive.core.process.notify;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * Interface needed by the notification process to generate notification messages.
 * 
 * @author Nico
 * 
 */
public interface INotificationMessageFactory {

	BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId);
}
