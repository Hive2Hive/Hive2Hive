package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkNode;
import org.hive2hive.core.network.NetworkManager;

public abstract class NetworkNode implements INetworkNode {

	private final INetworkConfiguration networkConfiguration;
	protected final NetworkManager networkManager;

	public NetworkNode(INetworkConfiguration networkConfiguration) {
		this.networkConfiguration = networkConfiguration;
		
		networkManager = new NetworkManager(networkConfiguration.getNodeID());
	}
	
	@Override
	public void connect() {
		networkManager.connect(networkConfiguration);
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}
}
