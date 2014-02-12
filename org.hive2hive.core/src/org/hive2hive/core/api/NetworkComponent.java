package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.INetworkComponent;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.network.NetworkManager;

public abstract class NetworkComponent implements INetworkComponent {

	private NetworkManager networkManager;
	
	@Override
	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected NetworkManager getNetworkManager() throws NoNetworkException {
		if (networkManager == null)
			throw new NoNetworkException();
		return networkManager;
	}
}