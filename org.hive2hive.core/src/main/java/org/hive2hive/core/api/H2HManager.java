package org.hive2hive.core.api;

import org.hive2hive.core.network.NetworkManager;

/**
 * Abstract base class for all API managers of the Hive2Hive project.
 * 
 * @author Christian
 * 
 */
public abstract class H2HManager {

	protected final NetworkManager networkManager;

	protected H2HManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}
}