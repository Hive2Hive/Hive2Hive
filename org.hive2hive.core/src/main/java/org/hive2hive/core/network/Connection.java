package org.hive2hive.core.network;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConficuration;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.MessageReplyHandler;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection {

	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	private static final int MAX_PORT = 65535;

	private final String nodeID;
	private final NetworkManager networkManager;

	private boolean isConnected;
	private Peer peer;
	private DefaultEventExecutorGroup eventExecutorGroup;

	public Connection(String nodeID, NetworkManager networkManager) {
		this.nodeID = nodeID;
		this.networkManager = networkManager;
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

		FutureDiscover futureDiscover = peer.discover().inetAddress(bootstrapAddress).ports(port).start();
		futureDiscover.awaitUninterruptibly();

		if (futureDiscover.isSuccess()) {
			logger.debug("Discovery successful: Outside address is '{}'.", futureDiscover.getPeerAddress());
		} else {
			logger.warn("Discovery failed: {}.", futureDiscover.getFailedReason());
			peer.shutdown();
			isConnected = false;
			return false;
		}

		FutureBootstrap futureBootstrap = peer.bootstrap().setInetAddress(bootstrapAddress).setPorts(port).start();
		futureBootstrap.awaitUninterruptibly();

		if (futureBootstrap.isSuccess()) {
			logger.debug("Bootstrapping successful. Bootstrapped to '{}'.", bootstrapAddress.getHostAddress());
			return true;
		} else {
			logger.warn("Bootstrapping failed: {}.", futureBootstrap.getFailedReason());
			peer.shutdown();
			isConnected = false;
			return false;
		}
	}

	/**
	 * Disconnects a peer from the network.
	 * 
	 * @return True, if disconnection was successful, false otherwise.
	 */
	public boolean disconnect() {
		boolean isDisconnected = true;
		if (isConnected) {
			// TODO check whether this always shuts down the whole network or if the peer just leaves
			isDisconnected = peer.shutdown().awaitUninterruptibly(H2HConstants.DISCONNECT_TIMEOUT_MS);
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

	public Peer getPeer() {
		return peer;
	}

	private boolean createPeer() {
		int port = H2HConstants.H2H_PORT;
		logger.debug("Start searching for a free port");
		while (!NetworkUtils.isPortAvailable(port)) {
			if (port > MAX_PORT) {
				logger.error("Could not find any free port");
				return false;
			}

			port++;
		}
		logger.debug("Found free port {}.", port);

		// configure the thread handling internally, callback can be blocking
		eventExecutorGroup = new DefaultEventExecutorGroup(H2HConstants.NUM_OF_NETWORK_THREADS);

		ChannelClientConfiguration clientConfig = PeerMaker.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(new H2HSignatureFactory());
		clientConfig.pipelineFilter(new PeerMaker.EventExecutorGroupFilter(eventExecutorGroup));

		ChannelServerConficuration serverConfig = PeerMaker.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(new H2HSignatureFactory());
		serverConfig.pipelineFilter(new PeerMaker.EventExecutorGroupFilter(eventExecutorGroup));

		try {
			peer = new PeerMaker(Number160.createHash(nodeID)).ports(port).setEnableIndirectReplication(true)
					.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).makeAndListen();
		} catch (IOException e) {
			logger.error("Exception while creating a peer: ", e);
			return false;
		}

		// override the put method for validation tasks
		peer.getPeerBean().storage(new H2HStorageMemory());
		// attach a reply handler for messages
		peer.setObjectDataReply(new MessageReplyHandler(networkManager));

		return true;
	}
}
