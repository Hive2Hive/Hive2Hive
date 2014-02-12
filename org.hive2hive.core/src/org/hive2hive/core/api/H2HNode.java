package org.hive2hive.core.api;

import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkComponent;
import org.hive2hive.core.network.NetworkManager;

public class H2HNode {

	//TODO atm, this class is just a wrapper for the NetworkManager
	private NetworkManager networkManager;
	private final ProcessManager processManager;
	
	public H2HNode() {
		processManager = new ProcessManager(true);
	}

	public void connect(INetworkConfiguration networkConfiguration) {
		networkManager = new NetworkManager(networkConfiguration);
	}

	public void disconnect() {
		networkManager.disconnect();
	}

	public void attach(INetworkComponent component) {
		component.setNetworkManager(networkManager);
	}

}