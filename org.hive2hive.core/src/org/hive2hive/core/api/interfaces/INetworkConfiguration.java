package org.hive2hive.core.api.interfaces;

import java.net.InetAddress;

public interface INetworkConfiguration {

	String getNodeID();
	
	boolean isMasterPeer();

	InetAddress getBootstrapAddress();
	
	int getBootstrapPort();
}
