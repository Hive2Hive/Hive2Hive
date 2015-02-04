package org.hive2hive.core.api.configs;

import java.net.InetAddress;
import java.util.UUID;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

/**
 * Configures the network settings of the peer.
 * Works with the builder pattern style.
 * 
 * @author Nico
 * @author Chris
 * @author Seppi
 */
public class NetworkConfiguration implements INetworkConfiguration {

	private static final int AUTO_PORT = -1;

	private String nodeID = UUID.randomUUID().toString();
	private int port = AUTO_PORT;
	private boolean isInitialPeer = true;
	private InetAddress bootstrapAddress = null;
	private boolean isLocal = false;
	private Peer bootstrapPeer = null;
	private int bootstrapPort = H2HConstants.H2H_PORT;

	/**
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @return this instance
	 */
	public NetworkConfiguration setNodeId(String nodeID) {
		this.nodeID = nodeID;
		return this;
	}

	/**
	 * @param port defines the port to bind. Should be free or negative (autodetect)
	 * @return this instance
	 */
	public NetworkConfiguration setPort(int port) {
		this.port = port;
		return this;
	}

	/**
	 * Set when the peer is the first one in the network
	 * 
	 * @return this instance
	 */
	public NetworkConfiguration setInitial() {
		this.isInitialPeer = true;
		this.bootstrapAddress = null;
		return this;
	}

	/**
	 * @param bootstrapAddress the address to bootstrap to
	 * @return this instance
	 */
	public NetworkConfiguration setBootstrap(InetAddress bootstrapAddress) {
		return setBootstrap(bootstrapAddress, H2HConstants.H2H_PORT);
	}

	/**
	 * @param bootstrapPort the port to bootstrap to
	 * @return this instance
	 */
	public NetworkConfiguration setBootstrapPort(int bootstrapPort) {
		this.bootstrapPort = bootstrapPort;
		return this;
	}

	/**
	 * 
	 * @param bootstrapAddress the address to bootstrap to
	 * @param bootstrapPort the port to bootstrap to
	 * @return this instance
	 */
	public NetworkConfiguration setBootstrap(InetAddress bootstrapAddress, int bootstrapPort) {
		this.bootstrapAddress = bootstrapAddress;
		this.bootstrapPort = bootstrapPort;
		return this;
	}

	/**
	 * @param bootstrapPeer the initial local peer to bootstrap to. Note: this is just for testings
	 * @return this instance
	 */
	public NetworkConfiguration setBootstrapLocal(Peer bootstrapPeer) {
		this.bootstrapPeer = bootstrapPeer;
		return setLocal();
	}

	/**
	 * Set the peer to only connect locally
	 */
	public NetworkConfiguration setLocal() {
		this.isLocal = true;
		return this;
	}

	/**
	 * Create network configuration for initial peer with random node id
	 * 
	 * @return the network configuration
	 */
	public static NetworkConfiguration createInitial() {
		return createInitial(UUID.randomUUID().toString());
	}

	/**
	 * Create network configuration for initial peer with given node id.
	 * 
	 * @param nodeID defines the location of the peer in the DHT
	 * @return the network configuration
	 */
	public static NetworkConfiguration createInitial(String nodeID) {
		return new NetworkConfiguration().setNodeId(nodeID).setInitial().setPort(AUTO_PORT);
	}

	/**
	 * Create network configuration for 'normal' peer. The bootstrapping happens at the default port
	 * {@link H2HConstants#H2H_PORT}.
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the initial peer or any
	 *            other peer connected to the DHT.
	 * @return the network configuration
	 */
	public static NetworkConfiguration create(String nodeID, InetAddress bootstrapAddress) {
		return new NetworkConfiguration().setNodeId(nodeID).setPort(AUTO_PORT)
				.setBootstrap(bootstrapAddress, H2HConstants.H2H_PORT);
	}

	/**
	 * Create network configuration for 'normal' peer. The bootstrapping happens to the specified address and
	 * port
	 * 
	 * @param nodeID defines the location of the peer in the DHT. Should not be null
	 * @param bootstrapAddress the address to bootstrap to. This can be address of the initial peer or any
	 *            other peer connected to the DHT.
	 * @param bootstrapPort the port to bootstrap
	 * @return the network configuration
	 */
	public static NetworkConfiguration create(String nodeID, InetAddress bootstrapAddress, int bootstrapPort) {
		return new NetworkConfiguration().setNodeId(nodeID).setPort(AUTO_PORT).setBootstrap(bootstrapAddress, bootstrapPort);
	}

	/**
	 * Creates a local peer that is only able to bootstrap to a peer running on the same host.
	 * 
	 * @param nodeID the id of the peer to create
	 * @param initialPeer the peer to bootstrap to
	 * @return the network configuration for local peers
	 */
	public static NetworkConfiguration createLocalPeer(String nodeID, Peer initialPeer) {
		return new NetworkConfiguration().setNodeId(nodeID).setPort(AUTO_PORT).setBootstrapLocal(initialPeer);
	}

	/**
	 * Create a local initial peer. Regard that bootstrapping may only work for peers running on the same
	 * host.
	 * 
	 * @param nodeID the id of the initial peer
	 * @return the network configuration for local peers (initial)
	 */
	public static NetworkConfiguration createInitialLocalPeer(String nodeID) {
		return new NetworkConfiguration().setNodeId(nodeID).setPort(AUTO_PORT).setInitial().setLocal();
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

	@Override
	public int getPort() {
		return port;
	}

}
