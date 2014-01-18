package org.hive2hive.processes.framework.abstracts;

import org.hive2hive.core.network.NetworkManager;

public abstract class NetworkManagerContext {
	
	private final NetworkManager networkManager;
	
	public NetworkManagerContext(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}
	
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

}
