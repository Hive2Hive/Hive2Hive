package org.hive2hive.core.network;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * The NetworkManager provides methods for establishing a connection to the
 * network, to send messages, to put and get data into the network and provides
 * all peer informations.
 * 
 * @author Seppi
 */
public class NetworkManager {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(NetworkManager.class);

	private final String nodeId;
	private final Connection connection;
	private final MessageManager messageManager;

	public String getNodeId() {
		return nodeId;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public MessageManager getMessageManager() {
		return messageManager;
	}

	public PeerAddress getPeerAddress() {
		return getConnection().getPeer().getPeerAddress();
	}
	
	public NetworkManager() {
		// TODO give node id
		nodeId = "bla";
		connection = new Connection(nodeId, this);
		messageManager = new MessageManager(this);
	}

	/**
	 * Shutdown the connection to the p2p network.
	 */
	public void disconnect() {
		connection.disconnect();
		logger.debug(String.format("Peer '%s' is shutdown", nodeId));
	}

	/**
	 * Sends a given message to the peer which is responsible to given key.
	 * 
	 * @param aMessage
	 *            the message to send
	 */
	public void send(BaseMessage aMessage) {
		messageManager.send(aMessage);
	}

	/**
	 * Sends a given message directly (TCP) to the peer with the given address.
	 * 
	 * @param aMessge
	 *            the message to send
	 * @see {@link MessageManager#send(AsynchronousMessage)}
	 */
	public void sendDirect(BaseDirectMessage aMessage) {
		messageManager.send(aMessage);
	}

}
