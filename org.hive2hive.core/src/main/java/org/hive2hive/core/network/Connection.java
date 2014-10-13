package org.hive2hive.core.network;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConficuration;
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

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.MessageReplyHandler;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.hive2hive.core.security.IH2HEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a <code>TomP2P</code> peer. Provides methods for discovering and bootstrapping to other peers as
 * well as shutdown.
 * 
 * @author Seppi, Nico
 */
public class Connection {

	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	private static final int MAX_PORT = 65535;

	private final String nodeID;
	private final NetworkManager networkManager;
	private final IH2HEncryption encryption;

	private boolean isConnected;
	private PeerDHT peerDHT;
	private DefaultEventExecutorGroup eventExecutorGroup;

	public Connection(String nodeID, NetworkManager networkManager, IH2HEncryption encryption) {
		this.nodeID = nodeID;
		this.networkManager = networkManager;
		this.encryption = encryption;
	}

	/**
	 * Creates a peer and connects it to the network.
	 * 
	 * @return True, if the peer creation and connection was successful, false otherwise
	 */
	public boolean connect() {
		if (isConnected) {
			logger.warn("Peer is already connected.");
			return false;
		}

		if (createPeer()) {
			isConnected = true;
			logger.debug("Peer successfully created and connected.");
			return true;
		}
		return false;
	}

	/**
	 * Creates a peer and connects it to the network.
	 * 
	 * @param bootstrapAddress Bootstrap IP address.
	 * @return true, if peer creation and bootstrapping was successful, false otherwise.
	 */
	public boolean connect(InetAddress bootstrapAddress) {
		return connect(bootstrapAddress, H2HConstants.H2H_PORT);
	}

	/**
	 * Creates a peer and connects it to the network.
	 * 
	 * @param bootstrapAddress Bootstrap IP address.
	 * @param port Bootstrap port.
	 * @return True, if peer creation and bootstrapping was successful, false otherwise.
	 */
	public boolean connect(InetAddress bootstrapAddress, int port) {
		if (!connect()) {
			return false;
		}

		FutureDiscover futureDiscover = peerDHT.peer().discover().inetAddress(bootstrapAddress).ports(port).start();
		futureDiscover.awaitUninterruptibly();

		if (futureDiscover.isSuccess()) {
			logger.debug("Discovery successful. Outside address is '{}'.", futureDiscover.peerAddress().inetAddress());
		} else {
			logger.warn("Discovery failed: {}.", futureDiscover.failedReason());
			peerDHT.shutdown().awaitUninterruptibly();
			isConnected = false;
			return false;
		}

		FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress(bootstrapAddress).ports(port).start();
		futureBootstrap.awaitUninterruptibly();

		if (futureBootstrap.isSuccess()) {
			logger.debug("Bootstrapping successful. Bootstrapped to '{}'.", bootstrapAddress.getHostAddress());
			return true;
		} else {
			logger.warn("Bootstrapping failed: {}.", futureBootstrap.failedReason());
			peerDHT.shutdown().awaitUninterruptibly();
			isConnected = false;
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
		if (isConnected) {
			// notify neighbors about shutdown
			peerDHT.peer().announceShutdown().start().awaitUninterruptibly();
			// shutdown the peer, giving a certain timeout
			isDisconnected = peerDHT.shutdown().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
			isConnected = !isDisconnected;

			if (isDisconnected) {
				logger.debug("Peer successfully disconnected.");
			} else {
				logger.warn("Peer disconnection failed.");
			}
		} else {
			logger.warn("Peer disconnection failed. Peer is not connected.");
		}

		if (eventExecutorGroup != null) {
			Future<?> shutdownGracefully = eventExecutorGroup.shutdownGracefully();
			shutdownGracefully.awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
			eventExecutorGroup = null;
		}

		return isDisconnected;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public PeerDHT getPeerDHT() {
		return peerDHT;
	}

	private PeerBuilder preparePeerBuilder() {
		int port = searchFreePort();

		// configure the thread handling internally, callback can be blocking
		eventExecutorGroup = new DefaultEventExecutorGroup(H2HConstants.NUM_OF_NETWORK_THREADS);

		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(new H2HSignatureFactory());
		clientConfig.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(eventExecutorGroup));

		ChannelServerConficuration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(new H2HSignatureFactory());
		serverConfig.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(eventExecutorGroup));
		serverConfig.ports(new Ports(port, port));

		return new PeerBuilder(Number160.createHash(nodeID)).ports(port).channelClientConfiguration(clientConfig)
				.channelServerConfiguration(serverConfig);
	}

	private void startReplication() {
		IndirectReplication replication = new IndirectReplication(peerDHT);
		// set replication factor
		replication.replicationFactor(H2HConstants.REPLICATION_FACTOR);
		// set replication frequency
		replication.intervalMillis(H2HConstants.REPLICATION_INTERVAL);
		// set kind of replication, default is 0Root
		if (H2HConstants.REPLICATION_STRATEGY.equals("nRoot")) {
			replication.nRoot();
		}
		// set flag to keep data, even when peer looses replication responsibility
		replication.keepData(true);
		// start the indirect replication
		replication.start();
	}

	private boolean createPeer() {
		try {
			H2HStorageMemory storageMemory = new H2HStorageMemory();
			peerDHT = new PeerBuilderDHT(preparePeerBuilder().start())
					.storage(new StorageMemory(H2HConstants.TTL_PERIOD, H2HConstants.MAX_VERSIONS_HISTORY))
					.storageLayer(storageMemory).start();
		} catch (IOException e) {
			logger.error("Exception while creating a peer: ", e);
			return false;
		}

		// attach a reply handler for messages
		peerDHT.peer().objectDataReply(new MessageReplyHandler(networkManager, encryption));

		// setup replication
		startReplication();

		return true;
	}

	/**
	 * Create a local master peer. <b>Important:</b> This is only for testing purposes!
	 * 
	 * @return <code>true</code> if everything went ok, <code>false</code> otherwise
	 */
	public boolean connectInternal() {
		return connectInternal(null);
	}

	/**
	 * Create a local peer. Bootstrap to local master peer. <b>Important:</b> This is only for testing
	 * purposes!
	 * 
	 * @param masterPeer
	 *            the newly created peer bootstraps to given local master peer
	 * @return <code>true</code> if everything went ok, <code>false</code> otherwise
	 */
	public boolean connectInternal(Peer masterPeer) {
		// disable peer verification (faster mutual acceptance)
		PeerMapConfiguration peerMapConfiguration = new PeerMapConfiguration(Number160.createHash(nodeID));
		peerMapConfiguration.peerVerification(false);
		// set higher peer map update frequency
		peerMapConfiguration.maintenance(new DefaultMaintenance(4, new int[] { 1 }));
		// only one try required to label a peer as offline
		peerMapConfiguration.offlineCount(1);
		peerMapConfiguration.shutdownTimeout(1);
		PeerMap peerMap = new PeerMap(peerMapConfiguration);

		try {
			H2HStorageMemory storageMemory = new H2HStorageMemory();
			peerDHT = new PeerBuilderDHT(preparePeerBuilder().masterPeer(masterPeer).peerMap(peerMap).start())
					.storage(new StorageMemory(H2HConstants.TTL_PERIOD, H2HConstants.MAX_VERSIONS_HISTORY))
					.storageLayer(storageMemory).start();
		} catch (IOException e) {
			logger.error("Exception while creating a local peer: ", e);
			return false;
		}

		// attach a reply handler for messages
		peerDHT.peer().objectDataReply(new MessageReplyHandler(networkManager, encryption));

		// setup replication
		startReplication();

		if (masterPeer != null) {
			// bootstrap to master peer
			FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().peerAddress(masterPeer.peerAddress()).start();
			futureBootstrap.awaitUninterruptibly();

			if (futureBootstrap.isSuccess()) {
				logger.debug("Bootstrapping successful. Bootstrapped to '{}'.", masterPeer.peerAddress());
				isConnected = true;
				return true;
			} else {
				logger.warn("Bootstrapping failed: {}.", futureBootstrap.failedReason());
				peerDHT.shutdown().awaitUninterruptibly();
				isConnected = false;
				return false;
			}
		} else {
			isConnected = true;
			return true;
		}
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
