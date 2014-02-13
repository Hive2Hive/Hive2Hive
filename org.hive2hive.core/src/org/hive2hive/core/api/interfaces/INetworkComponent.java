package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.network.NetworkManager;

public interface INetworkComponent {

	/**
	 * Provides the component with the necessary instance of {@link NetworkManager}.
	 * 
	 * @param networkManager
	 */
	void setNetworkManager(NetworkManager networkManager);
}
