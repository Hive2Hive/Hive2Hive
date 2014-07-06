package org.hive2hive.core.processes.context;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class NotifyProcessContext implements INotifyContext, IGetUserLocationsContext {

	private final INotifyContext providerContext;
	private final String userId;

	private Map<String, PublicKey> userPublicKeys;
	private Map<String, List<PeerAddress>> allLocations;
	private Locations locations;

	public NotifyProcessContext(INotifyContext providerContext, String userId) {
		this.providerContext = providerContext;
		this.userId = userId;
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
	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public String consumeUserId() {
		return userId;
	}

	public Locations consumeLocations() {
		return locations;
	}

}
