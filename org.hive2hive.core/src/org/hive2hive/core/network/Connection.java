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
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.MessageReplyHandler;

/**
 * This class creates a TomP2P peer and establishes a connection.
 * 
 * @author Seppi
 */
public class Connection {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(Connection.class);
	private DefaultEventExecutorGroup eventExecutorGroup;

	private Peer peer = null;
	private boolean isConnected = false;

	public Peer getPeer() {
		return peer;
	}

	public boolean isConnected() {
		return isConnected;
	}

	private final String nodeId;
	private final NetworkManager networkManager;

	/**
	 * The constructor for a connection.
	 * 
	 * @param nodeId
	 *            the id of the node
	 * @param networkManager
	 */
	public Connection(String nodeId, NetworkManager networkManager) {
		this.nodeId = nodeId;
		this.networkManager = networkManager;
	}

	/**
	 * Create a peer which will be the first node in the network (master).
	 * 
	 * @return <code>true</code> if creating master peer was successful, <code>false</code> if not
	 */
	public boolean connect() {
		if (isConnected) {
			logger.warn("Peer is already connected!");
			return false;
		}
		if (createPeer()) {
			isConnected = true;
			logger.debug("Master peer successfully created.");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Create a peer and bootstrap to a given peer through IP address
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful, <code>false</code> if not
	 */
	public boolean connect(InetAddress bootstrapInetAddress) {
		return connect(bootstrapInetAddress, H2HConstants.H2H_PORT);
	}

	/**
	 * Create a peer and bootstrap to a given peer through IP address and port
	 * number
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @param port
	 *            port number to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful, <code>false</code> if not
	 */
	public boolean connect(InetAddress bootstrapInetAddress, int port) {
		if (isConnected) {
			logger.warn("Peer is already connected.");
			return false;
		} else {
			logger.debug("Connecting...");
		}

		if (!createPeer())
			return false;

		FutureDiscover futureDiscover = peer.discover().inetAddress(bootstrapInetAddress).ports(port).start();
		futureDiscover.awaitUninterruptibly();

		if (futureDiscover.isSuccess()) {
			logger.debug(String.format("Successfully discovered, found that my outside address is: %s",
					futureDiscover.getPeerAddress()));
		} else {
			logger.warn(String.format("Failed discovering: %s", futureDiscover.getFailedReason()));
			peer.shutdown();
			isConnected = false;
			return false;
		}

		FutureBootstrap futureBootstrap = peer.bootstrap().setInetAddress(bootstrapInetAddress)
				.setPorts(port).start();
		futureBootstrap.awaitUninterruptibly();

		if (futureBootstrap.isSuccess()) {
			logger.debug(String.format("Successfully bootstraped to: %s",
					bootstrapInetAddress.getHostAddress()));
			isConnected = true;
			return true;
		} else {
			logger.warn(String.format("Failed bootstraping: %s", futureBootstrap.getFailedReason()));
			peer.shutdown();
			isConnected = false;
			return false;
		}
	}

	public void disconnect() {
		if (isConnected) {
			peer.shutdown().awaitUninterruptibly(10000);
			isConnected = false;
		} else {
			logger.warn("Peer is not connected. No disconnect.");
		}

		if (eventExecutorGroup != null) {
			Future<?> shutdownGracefully = eventExecutorGroup.shutdownGracefully();
			shutdownGracefully.awaitUninterruptibly(10000);
			eventExecutorGroup = null;
		}
	}

	private boolean createPeer() {
		try {
			int port = H2HConstants.H2H_PORT;
			// check if given port is available, if not increment it till
			// available
			while (NetworkUtils.isPortAvailable(port) == false)
				port++;

			eventExecutorGroup = new DefaultEventExecutorGroup(H2HConstants.NUM_OF_NETWORK_THREADS);
			// configure the thread handling internally. Callback can be blocking.
			ChannelClientConfiguration clientConfig = PeerMaker.createDefaultChannelClientConfiguration();
			clientConfig.pipelineFilter(new PeerMaker.EventExecutorGroupFilter(eventExecutorGroup));

			ChannelServerConficuration serverConfig = PeerMaker.createDefaultChannelServerConfiguration();
			serverConfig.pipelineFilter(new PeerMaker.EventExecutorGroupFilter(eventExecutorGroup));

			peer = new PeerMaker(Number160.createHash(nodeId)).ports(port).setEnableIndirectReplication(true)
					.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig)
					.makeAndListen();

			// override the put method for validation tasks
			peer.getPeerBean().storage(new H2HStorageMemory());
			// attach a reply handler for messages
			peer.setObjectDataReply(new MessageReplyHandler(networkManager));
			return true;
		} catch (IOException e) {
			logger.error(String.format("Exception during the creation of a peer: %s", e.getMessage()));
			return false;
		}
	}
}