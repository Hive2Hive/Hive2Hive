package org.hive2hive.core.processes.implementations.context;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public class NotifyProcessContext implements IConsumeNotificationFactory, IConsumeLocations, IProvideLocations {

	private final IConsumeNotificationFactory providerContext;
	private final Set<PeerAddress> unreachablePeers;
	private Map<String, PublicKey> userPublicKeys;
	private Map<String, List<PeerAddress>> allLocations;
	private Locations locations;

	public NotifyProcessContext(IConsumeNotificationFactory providerContext) {
		this.providerContext = providerContext;
		this.unreachablePeers = new HashSet<PeerAddress>();
	}

	public void setUserPublicKeys(Map<String, PublicKey> keys) {
		this.userPublicKeys = keys;
	}

	public Map<String, PublicKey> getUserPublicKeys() {
		return userPublicKeys;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return providerContext.consumeMessageFactory();
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return providerContext.consumeUsersToNotify();
	}

	public void setAllLocations(Map<String, List<PeerAddress>> allLocations) {
		this.allLocations = allLocations;
	}

	public Map<String, List<PeerAddress>> getAllLocations() {
		return allLocations;
	}

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;

	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}
}
