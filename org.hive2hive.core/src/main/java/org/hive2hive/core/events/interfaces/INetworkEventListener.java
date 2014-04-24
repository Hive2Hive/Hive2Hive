package org.hive2hive.core.events.interfaces;


public interface INetworkEventListener extends IEventListener {

	void onConnectionSuccess(INetworkEvent event);
	
	void onConnectionFailure(INetworkEvent event);
	
	void onDisconnectionSuccess(INetworkEvent event);
	
	void onDisconnectionFailure(INetworkEvent event);
}
