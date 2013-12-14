package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.ProcessContext;

public class NotifyPeersProcessContext extends ProcessContext {

	private final Set<String> users;
	private final INotificationMessageFactory messageFactory;
	private final Set<PeerAddress> unreachableOwnPeers;
	private Map<String, PublicKey> keys;

	public NotifyPeersProcessContext(Process process, Set<String> users,
			INotificationMessageFactory messageFactory) {
		super(process);
		this.users = users;
		this.messageFactory = messageFactory;
		unreachableOwnPeers = new HashSet<PeerAddress>();
	}

	public Set<String> getUsers() {
		return users;
	}

	public INotificationMessageFactory getMessageFactory() {
		return messageFactory;
	}

	public void setUserPublicKeys(Map<String, PublicKey> keys) {
		this.keys = keys;
	}

	public Map<String, PublicKey> getUserPublicKeys() {
		return keys;
	}

	public void addUnreachableLocation(PeerAddress unreachable) {
		unreachableOwnPeers.add(unreachable);
	}

	public Set<PeerAddress> getUnreachableOwnPeers() {
		return unreachableOwnPeers;
	}
}
