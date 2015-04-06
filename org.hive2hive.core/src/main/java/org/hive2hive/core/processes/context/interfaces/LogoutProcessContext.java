package org.hive2hive.core.processes.context.interfaces;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

public class LogoutProcessContext {

	private Set<PeerAddress> recipients;

	public void provideNotificationRecipients(Set<PeerAddress> recipients) {
		this.recipients = recipients;
	}

	public Set<PeerAddress> consumeNotificationRecipients() {
		return recipients;
	}
}
