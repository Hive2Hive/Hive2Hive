package org.hive2hive.core.events.framework.abstracts;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.framework.interfaces.INetworkEvent;

public abstract class NetworkEvent implements INetworkEvent {

	private final INetworkConfiguration networkConfiguration;

	public NetworkEvent(INetworkConfiguration networkConfiguration) {
		this.networkConfiguration = networkConfiguration;
	}
	
	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}

}
