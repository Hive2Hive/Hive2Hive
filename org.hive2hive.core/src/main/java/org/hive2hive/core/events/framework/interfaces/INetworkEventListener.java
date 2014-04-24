package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventListener;
import org.hive2hive.core.events.framework.interfaces.network.IConnectionEvent;


public interface INetworkEventListener extends IEventListener {

	void onConnectionSuccess(IConnectionEvent event);
	
	void onConnectionFailure(IConnectionEvent event);
	
	void onDisconnectionSuccess(IConnectionEvent event);
	
	void onDisconnectionFailure(IConnectionEvent event);
}
