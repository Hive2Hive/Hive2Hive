package org.hive2hive.core.api;

import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkComponent;
import org.hive2hive.core.network.NetworkManager;

public class H2HNode {

	// TODO atm, this class is just a wrapper for the NetworkManager
	private final NetworkManager networkManager;
	
	public H2HNode(INetworkConfiguration networkConfiguration) {
		networkManager = new NetworkManager(networkConfiguration);
	}
	
	public void attach(INetworkComponent component) {
		component.setNetworkManager(networkManager);
	}

	public void connect() {
		networkManager.connect();
	}

	public void disconnect() {
		networkManager.disconnect();
	}

}