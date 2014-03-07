package org.hive2hive.core.api.configs;

import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

/**
 * Configures the network settings of the peer.
 * 
 * @author Nico, Chris
 * 
 */
public class NetworkConfiguration implements INetworkConfiguration {

	private String nodeID;
	private boolean isMasterPeer;
	private InetAddress bootstrapAddress;
	private int bootstrapPort;

	/**
	 * Create network configuration
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param isMasterPeer true when the peer is the first one in the network
	 * @param bootstrapAddress the address to bootstrap to
	 * @param bootstrapPort the port to bootstrap and to listen to
	 */
	private NetworkConfiguration(String nodeID, boolean isMasterPeer, InetAddress bootstrapAddress,
			int bootstrapPort) {
		this.nodeID = nodeID;
		this.isMasterPeer = isMasterPeer;
		this.bootstrapAddress = bootstrapAddress;
		this.bootstrapPort = bootstrapPort;
	}

	/**
	 * Create network configuration for master peer with random node id
	 * 
	 * @return the network configuration
	 */
	public static INetworkConfiguration create() {
		return create(UUID.randomUUID().toString());
	}

	/**
	 * Create network configuration for master peer with given node id.
	 * 
	 * @param nodeID defines the location of the peer in the DHT
	 * @return the network configuration
	 */
	public static INetworkConfiguration create(String nodeID) {
		return new NetworkConfiguration(nodeID, true, null, -1);
	}

	/**
	 * Create network configuration for 'normal' peer. The connection happens at the default port
	 * {@link H2HConstants#H2H_PORT}.
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the master peer or any
	 *            other peer connected to the DHT.
	 * @return the network configuration
	 */
	public static INetworkConfiguration create(String nodeID, InetAddress bootstrapAddress) {
		return create(nodeID, bootstrapAddress, -1);
	}

	/**
	 * Create network configuration for 'normal' peer and a manually given port.
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the master peer or any
	 *            other peer connected to the DHT.
	 * @param bootstrapPort the port the peer should bootstrap and then later listen to.
	 * @return the network configuration
	 */
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
