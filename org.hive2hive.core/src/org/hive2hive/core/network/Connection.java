package org.hive2hive.core.network;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.MessageReplyHandler;

/**
 * This class creates a TomP2P peer and establishes a connection.
 * 
 * @author Seppi
 */
public class Connection {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(Connection.class);

	private Peer peer = null;

	public Peer getPeer() {
		return peer;
	}

	private final String nodeId;
	private final NetworkManager networkManager;

	/**
	 * The constructor for a connection.
	 * 
	 * @param aNodeId the id of the node
	 * @param aNetworkManager
	 */
	public Connection(String aNodeId, NetworkManager aNetworkManager) {
		nodeId = aNodeId;
		networkManager = aNetworkManager;
	}

	/**
	 * 
	 * Create a peer which will be the first node in the network (master).
	 * 
	 * @return <code>true</code> if creating master peer was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect() {
		return createPeer();
	}

	/**
	 * Create a peer and bootstrap to a given peer through IP address and port
	 * number
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @param aPort
	 *            port number to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect(InetAddress bootstrapInetAddress, int aPort) {
		if (!createPeer())
			return false;

		FutureDiscover futureDiscover = peer.discover()
				.setInetAddress(bootstrapInetAddress).setPorts(aPort).start();
		futureDiscover.awaitUninterruptibly();

		if (futureDiscover.isSuccess()) {
			logger.debug(String
					.format("Successfully discovered, found that my outside address is: %s",
							futureDiscover.getPeerAddress()));
		} else {
			logger.warn(String.format("Failed discovering: %s",
					futureDiscover.getFailedReason()));
			peer.shutdown();
			return false;
		}

		FutureBootstrap futureBootstrap = peer.bootstrap()
				.setInetAddress(bootstrapInetAddress).setPorts(aPort).start();
		futureBootstrap.awaitUninterruptibly();

		if (futureBootstrap.isSuccess()) {
			logger.debug(String.format("Successfully bootstraped to: %s",
					bootstrapInetAddress.getHostAddress()));
		} else {
			logger.warn(String.format("Failed bootstraping: %s",
					futureDiscover.getFailedReason()));
			peer.shutdown();
		}

		return futureBootstrap.isSuccess();
	}

	/**
	 * Create a peer and bootstrap to a given peer through IP address
	 * 
	 * @param aBootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect(InetAddress aBootstrapInetAddress) {
		return connect(aBootstrapInetAddress, H2HConstants.H2H_PORT);
	}

	public void disconnect() {
		peer.shutdown();
	}

	private boolean createPeer() {
		try {
			int port = H2HConstants.H2H_PORT;
			// check if given port is available, if not increment it till
			// available
			while (NetworkUtils.isPortAvailable(port) == false)
				port++;

			peer = new PeerMaker(Number160.createHash(nodeId)).setPorts(port)
					.setEnableIndirectReplication(true).makeAndListen();
			// override the put method for validation tasks
			peer.getPeerBean().setStorage(new H2HStorageMemory(networkManager));
			// attach a reply handler for messages
			peer.setObjectDataReply(new MessageReplyHandler(networkManager));
			return true;
		} catch (IOException e) {
			logger.error(String.format(
					"Exception during the creation of a peer: %s",
					e.getMessage()));
			return false;
		}
	}
}