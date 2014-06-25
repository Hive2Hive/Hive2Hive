package org.hive2hive.core.api.configs;

import java.net.InetAddress;
import java.util.UUID;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

/**
 * Configures the network settings of the peer.
 * 
 * @author Nico, Chris, Seppi
 */
public class NetworkConfiguration implements INetworkConfiguration {

	private String nodeID;
	private boolean isInitialPeer;
	private InetAddress bootstrapAddress;
	private boolean isLocal;
	private Peer bootstrapPeer;
	private int bootstrapPort;

	/**
	 * Create network configuration
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param isInitialPeer true when the peer is the first one in the network
	 * @param bootstrapAddress the address to bootstrap to
	 * @param isLocal true if peer will run only locally
	 * @param bootstrapPort the port to bootstrap and to listen to
	 * @param masterPeer the local peer to bootstrap
	 */
	private NetworkConfiguration(String nodeID, boolean isInitialPeer, InetAddress bootstrapAddress, int bootstrapPort,
			boolean isLocal, Peer masterPeer) {
		this.nodeID = nodeID;
		this.isInitialPeer = isInitialPeer;
		this.bootstrapAddress = bootstrapAddress;
		this.isLocal = isLocal;
		this.bootstrapPort = bootstrapPort;
		this.bootstrapPeer = masterPeer;
	}

	/**
	 * Create network configuration for initial peer with random node id
	 * 
	 * @return the network configuration
	 */
	public static INetworkConfiguration create() {
		return create(UUID.randomUUID().toString());
	}

	/**
	 * Create network configuration for initial peer with given node id.
	 * 
	 * @param nodeID defines the location of the peer in the DHT
	 * @return the network configuration
	 */
	public static INetworkConfiguration create(String nodeID) {
		return new NetworkConfiguration(nodeID, true, null, -1, false, null);
	}

	/**
	 * Create network configuration for 'normal' peer. The connection happens at the default port
	 * {@link H2HConstants#H2H_PORT}.
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the initial peer or any
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
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the initial peer or any
	 *            other peer connected to the DHT.
	 * @param bootstrapPort the port the peer should bootstrap and then later listen to.
	 * @return the network configuration
	 */
	public static INetworkConfiguration create(String nodeID, InetAddress bootstrapAddress, int bootstrapPort) {
		return new NetworkConfiguration(nodeID, false, bootstrapAddress, bootstrapPort, false, null);
	}

	public static INetworkConfiguration createLocalPeer(String nodeID, Peer masterPeer) {
		return new NetworkConfiguration(nodeID, false, null, -1, true, masterPeer);
	}
	
	public static INetworkConfiguration createLocalMasterPeer(String nodeID) {
		return new NetworkConfiguration(nodeID, false, null, -1, true, null);
	}

	@Override
	public String getNodeID() {
		return nodeID;
	}

	@Override
	public boolean isInitialPeer() {
		return isInitialPeer;
	}

	@Override
	public InetAddress getBootstrapAddress() {
		return bootstrapAddress;
	}

	@Override
	public boolean isBootstrappingLocaly() {
		return bootstrapPeer != null;
	}

	@Override
	public Peer getBootstapPeer() {
		return bootstrapPeer;
	}

	@Override
	public int getBootstrapPort() {
		return bootstrapPort;
	}

	@Override
	public boolean isLocal() {
		return isLocal;
	}

}
