package org.hive2hive.core.network.messages.usermessages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * An encrypted and signed message which is stored in the DHT. The user message
 * will be stored in a queue (see {@link UserMessageQueue}). This allows an asynchronous communication between
 * users (e.g. receiving user is currently offline).
 * 
 * @author Christian, Nico
 * 
 */
public abstract class UserMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 53893717112672279L;

	public UserMessage(PeerAddress targetPeerAddress) {
		super(targetPeerAddress);
	}
}
