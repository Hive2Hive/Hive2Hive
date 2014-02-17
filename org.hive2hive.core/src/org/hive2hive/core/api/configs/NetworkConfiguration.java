package org.hive2hive.core.api.configs;

import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;

public class NetworkConfiguration implements INetworkConfiguration {

	private String nodeID;
	private boolean isMasterPeer;
	private InetAddress bootstrapAddress;
	private int bootstrapPort;

	private NetworkConfiguration(String nodeID, boolean isMasterPeer, InetAddress bootstrapAddress, int bootstrapPort) {
		this.nodeID = nodeID;
		this.isMasterPeer = isMasterPeer;
		this.bootstrapAddress = bootstrapAddress;
		this.bootstrapPort = bootstrapPort;
	}

	public static INetworkConfiguration create() {
		return create(UUID.randomUUID().toString());
	}
	
	public static INetworkConfiguration create(String nodeID) {
		return new NetworkConfiguration(nodeID, true, null, -1);
	}

	public static INetworkConfiguration create(String nodeID, InetAddress bootstrapAddress) {
		return create(nodeID, bootstrapAddress, -1);
	}
	
	public static INetworkConfiguration create(String nodeID, InetAddress bootstrapAddress, int bootstrapPort) {
		return new NetworkConfiguration(nodeID, false, bootstrapAddress, bootstrapPort);
	}
	
	@Override
	public String getNodeID() {
		return nodeID;
	}

	@Override
	public boolean isMasterPeer() {
		return isMasterPeer;
	}

	@Override
	public InetAddress getBootstrapAddress() {
		return bootstrapAddress;
	}

	@Override
	public int getBootstrapPort() {
		return bootstrapPort;
	}

}
