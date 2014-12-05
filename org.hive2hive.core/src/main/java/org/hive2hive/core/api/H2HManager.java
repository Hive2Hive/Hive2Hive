package org.hive2hive.core.api;

import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.network.NetworkManager;

/**
 * Abstract base class for all API managers of the Hive2Hive project.
 * 
 * @author Christian
 * 
 */
public abstract class H2HManager {

	protected final NetworkManager networkManager;
	protected final EventBus eventBus;

	protected H2HManager(NetworkManager networkManager, EventBus eventBus) {
		this.networkManager = networkManager;
		this.eventBus = eventBus;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}
}