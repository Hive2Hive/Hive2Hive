package org.hive2hive.core.events;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.interfaces.INetworkEvent;

public class NetworkEvent implements INetworkEvent {

	private final INetworkConfiguration networkConfiguration;

	public NetworkEvent(INetworkConfiguration networkConfiguration) {
		this.networkConfiguration = networkConfiguration;
	}
	
	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}

}
