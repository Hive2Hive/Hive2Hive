package org.hive2hive.core.api.interfaces;

public interface INetworkNode {

	void connect();
	
	void disconnect();
	
	INetworkConfiguration getNetworkConfiguration();
}
