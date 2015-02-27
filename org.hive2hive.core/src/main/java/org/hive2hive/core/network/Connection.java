package org.hive2hive.core.network;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.Ports;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.DefaultMaintenance;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerMap;
import net.tomp2p.peers.PeerMapConfiguration;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.replication.SlowReplicationFilter;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.MessageReplyHandler;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.hive2hive.core.serializer.IH2HSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a <code>TomP2P</code> peer. Provides methods for discovering and bootstrapping to other peers as
 * well as shutdown.
 * 
 * @author Seppi, Nico
 */
public class Connection implements IPeerHolder {

	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	private static final int MAX_PORT = 65535;

	private final MessageReplyHandler messageReplyHandler;
	private PeerDHT peerDHT;

	public Connection(NetworkManager networkManager, IH2HSerialize serializer) {
		this.messageReplyHandler = new MessageReplyHandler(networkManager, serializer);
	}

	/**
	 * Creates a peer and connects it to the network.
	 * 
	 * @param nodeID the id of the network node (should be unique among the network)
	 * @param port the port to bind to (or negative to bind automatically a default port)
	 * @return <code>true</code>, if the peer creation and connection was successful, otherwise
	 *         <code>false</code>
	 */
	public boolean connect(String nodeID, int port) {
		if (isConnected()) {
			logger.warn("Peer is already connected.");
			return false;
		}

		return createPeer(nodeID, port);
	}

	/**
	 * Uses the given peer and does not bootstrap any further
	 * 
	 * @param peer the peer
	 * @param startReplication whether replication should be started
	 * @return <code>true</code> if the given peer is valid, otherwise <code>false</code>.
	 */
	public boolean connect(PeerDHT peer, boolean startReplication) {
		if (isConnected()) {
			logger.warn("Peer is already connected.");
			return false;
		} else if (peer.peer().isShutdown()) {
			logger.warn("Peer is already shut down.");
			return false;
		}
		this.peerDHT = peer;

		// attach a reply handler for messages
		peerDHT.peer().objectDataReply(messageReplyHandler);

		if (startReplication) {
			startReplication();
		}

		return true;
	}

	/**
	 * Bootstraps the connected peer to the network
	 * 
	 * @param bootstrapAddress Bootstrap IP address.
	 * @param port Bootstrap port.
	 * @return <code>true</code>, if bootstrapping was successful, <code>false</code> otherwise.
	 */
	public boolean bootstrap(InetAddress bootstrapAddress, int port) {
		if (!isConnected()) {
			logger.warn("Build peer first!");
			return false;
		}

		FutureDiscover futureDiscover = peerDHT.peer().discover().inetAddress(bootstrapAddress).ports(port).start();
		futureDiscover.awaitUninterruptibly(H2HConstants.DISCOVERY_TIMEOUT_MS);

		if (futureDiscover.isSuccess()) {
			logger.debug("Discovery successful. Outside address is '{}'.", futureDiscover.peerAddress().inetAddress());
		} else {
			logger.warn("Discovery failed: {}.", futureDiscover.failedReason());
		}

		FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress(bootstrapAddress).ports(port).start();
		futureBootstrap.awaitUninterruptibly(H2HConstants.BOOTSTRAPPING_TIMEOUT_MS);

		if (futureBootstrap.isSuccess()) {
			logger.debug("Bootstrapping successful. Bootstrapped to '{}'.", bootstrapAddress.getHostAddress());
			return true;
		} else {
			logger.warn("Bootstrapping failed: {}.", futureBootstrap.failedReason());
			peerDHT.shutdown().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
			return false;
		}
	}

	/**
	 * Disconnects a peer from the network.
	 * 
	 * @return <code>true</code>, if disconnection was successful, <code>false</code> otherwise.
	 */
	public boolean disconnect() {
		boolean isDisconnected = true;
		if (isConnected()) {
			// notify neighbors about shutdown
			peerDHT.peer().announceShutdown().start().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
			// shutdown the peer, giving a certain timeout
			isDisconnected = peerDHT.shutdown().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);

			if (isDisconnected) {
				logger.debug("Peer successfully disconnected.");
			} else {
				logger.warn("Peer disconnection failed.");
			}
		} else {
			logger.warn("Peer disconnection failed. Peer is not connected.");
		}

		return isDisconnected;
	}

	/**
	 * Create a local peer. Bootstrap to local master peer. Replication is not started!<br>
	 * <b>Important:</b> This is only for testing purposes!
	 * 
	 * @param nodeId the id of the network node (should be unique among the network)
	 * @param masterPeer
	 *            the newly created peer bootstraps to given local master peer. Can be <code>null</code> to
	 *            create a new network.
	 * @return <code>true</code> if everything went ok, <code>false</code> otherwise
	 */
	public boolean connectInternal(String nodeId, int port, Peer masterPeer) {
		// disable peer verification (faster mutual acceptance)
		PeerMapConfiguration peerMapConfiguration = new PeerMapConfiguration(Number160.createHash(nodeId));
		peerMapConfiguration.peerVerification(false);
		// set higher peer map update frequency
		peerMapConfiguration.maintenance(new DefaultMaintenance(4, new int[] { 1 }));
		// only one try required to label a peer as offline
		peerMapConfiguration.offlineCount(1);
		peerMapConfiguration.shutdownTimeout(1);
		PeerMap peerMap = new PeerMap(peerMapConfiguration);

		try {
			H2HStorageMemory storageMemory = new H2HStorageMemory();
			peerDHT = new PeerBuilderDHT(preparePeerBuilder(nodeId, port).masterPeer(masterPeer).peerMap(peerMap).start())
					.storage(new StorageMemory(H2HConstants.TTL_PERIOD, H2HConstants.MAX_VERSIONS_HISTORY))
					.storageLayer(storageMemory).start();
		} catch (IOException e) {
			logger.error("Exception while creating a local peer: ", e);
			return false;
		}

		// attach a reply handler for messages
		peerDHT.peer().objectDataReply(messageReplyHandler);

		if (masterPeer != null) {
			// bootstrap to master peer
			FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().peerAddress(masterPeer.peerAddress()).start();
			futureBootstrap.awaitUninterruptibly(H2HConstants.BOOTSTRAPPING_TIMEOUT_MS);

			if (futureBootstrap.isSuccess()) {
				logger.debug("Bootstrapping successful. Bootstrapped to '{}'.", masterPeer.peerAddress());
				return true;
			} else {
				logger.warn("Bootstrapping failed: {}.", futureBootstrap.failedReason());
				peerDHT.shutdown().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean isConnected() {
		return peerDHT != null && !peerDHT.peer().isShutdown();
	}

	@Override
	public PeerDHT getPeer() {
		return peerDHT;
	}

	/**
	 * @return the reply handler if a direct message is sent
	 */
	public MessageReplyHandler getMessageReplyHandler() {
		return messageReplyHandler;
	}

	private PeerBuilder preparePeerBuilder(String nodeID, int port) {
		int bindPort = port < 0 ? searchFreePort() : port;

		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(new H2HSignatureFactory());

		ChannelServerConfiguration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(new H2HSignatureFactory());
		serverConfig.ports(new Ports(bindPort, bindPort));

		// raise timeouts
		serverConfig.connectionTimeoutTCPMillis(15 * 1000);
		serverConfig.idleTCPSeconds(15 * 1000);
		serverConfig.idleUDPSeconds(15 * 1000);

		// listen on any interfaces (see https://github.com/Hive2Hive/Hive2Hive/issues/117)
		Bindings bindings = new Bindings().listenAny();

		return new PeerBuilder(Number160.createHash(nodeID)).ports(bindPort).bindings(bindings)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig);
	}

	private boolean createPeer(String nodeId, int port) {
		try {
			H2HStorageMemory storageMemory = new H2HStorageMemory();
			peerDHT = new PeerBuilderDHT(preparePeerBuilder(nodeId, port).start())
					.storage(new StorageMemory(H2HConstants.TTL_PERIOD, H2HConstants.MAX_VERSIONS_HISTORY))
					.storageLayer(storageMemory).start();
		} catch (IOException e) {
			logger.error("Exception while creating a peer: ", e);
			return false;
		}

		// attach a reply handler for messages
		peerDHT.peer().objectDataReply(messageReplyHandler);

		// setup replication
		startReplication();

		logger.debug("Peer successfully created and connected.");
		return true;
	}

	private void startReplication() {
		IndirectReplication replication = new IndirectReplication(peerDHT);
		// set replication factor
		replication.replicationFactor(H2HConstants.REPLICATION_FACTOR);
		// set replication frequency
		replication.intervalMillis(H2HConstants.REPLICATION_INTERVAL_MS);
		// set kind of replication, default is 0Root
		if (H2HConstants.REPLICATION_STRATEGY.equals("nRoot")) {
			replication.nRoot();
		}
		// set flag to keep data, even when peer looses replication responsibility
		replication.keepData(true);
		// add replication filter for slow peers
		replication.addReplicationFilter(new SlowReplicationFilter());
		// start the indirect replication
		replication.start();
	}

	/**
	 * Searches for open ports, starting at {@link H2HConstants#H2H_PORT}.
	 * 
	 * @return the free port or -1 if none was found.
	 */
	private int searchFreePort() {
		int port = H2HConstants.H2H_PORT;
		while (!NetworkUtils.isPortAvailable(port)) {
			if (port > MAX_PORT) {
				logger.error("Could not find any free port");
				return -1;
			}

			port++;
		}
		logger.debug("Found free port {}.", port);
		return port;
	}
}
