package org.hive2hive.core.events.implementations;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.framework.abstracts.NetworkEvent;
import org.hive2hive.core.events.framework.interfaces.network.IConnectionEvent;

public class ConnectionEvent extends NetworkEvent implements IConnectionEvent {

	public ConnectionEvent(INetworkConfiguration networkConfiguration) {
		super(networkConfiguration);
	}

}
