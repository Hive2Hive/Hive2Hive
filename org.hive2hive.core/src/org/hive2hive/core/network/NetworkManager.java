package org.hive2hive.core.network;

import java.net.InetAddress;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataWrapper;
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
	private final DataManager dataManager;

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
	
	public NetworkManager(String nodeId) {
		this.nodeId = nodeId;
		connection = new Connection(nodeId, this);
		messageManager = new MessageManager(this);
		dataManager = new DataManager(this);
	}
	
	/**
	 * Create a peer which will be the first node in the network (master).
	 * 
	 * @return <code>true</code> if creating master peer was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect(){
		return connection.connect();
	}
	
	/**
	 * Create a peer and bootstrap to a given peer through IP address
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect(InetAddress bootstrapInetAddress) {
		return connection.connect(bootstrapInetAddress);
	}
	
	/**
	 * Create a peer and bootstrap to a given peer through IP address and port
	 * number
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @param port
	 *            port number to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful,
	 *         <code>false</code> if not
	 */
	public boolean connect(InetAddress bootstrapInetAddress, int port) {
		return connection.connect(bootstrapInetAddress, port);
	}

	/**
	 * Shutdown the connection to the p2p network.
	 */
	public void disconnect() {
		if (!connection.isConnected())
			return;
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
		if (!connection.isConnected())
			return;
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
		if (!connection.isConnected())
			return;
		messageManager.send(aMessage);
	}

	/**
	 * Stores the content into the DHT at the location under the given content
	 * key
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @param wrapper
	 *            the wrapper containing the content to be stored
	 * @return the future
	 */
	public FutureDHT putGlobal(String locationKey, String contentKey,
			DataWrapper wrapper) {
		if (!connection.isConnected())
			return null;
		return dataManager.putGlobal(locationKey, contentKey, wrapper);
	}

	/**
	 * Loads the content with the given location and content keys from the
	 * DHT.</br> <b>Important:</b> This method blocks till the load succeeded.
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @return the desired content from the wrapper
	 */
	public Object getGlobal(String locationKey, String contentKey) {
		if (!connection.isConnected())
			return null;
		return dataManager.getGlobal(locationKey, contentKey);
	}

	/**
	 * Stores the given content with the key in the storage of the peer.</br>
	 * The content key allows to store several objects for the same key.
	 * <b>Important:</b> This method blocks till the storage succeeded.
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @param wrapper
	 *            the wrapper containing the content to be stored
	 */
	public void putLocal(String locationKey, String contentKey,
			DataWrapper wrapper) {
		if (!connection.isConnected())
			return;
		dataManager.putLocal(locationKey, contentKey, wrapper);
	}

	/**
	 * Loads the content with the key directly from the storage of the peer
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @return the desired content from the wrapper
	 */
	public Object getLocal(String locationKey, String contentKey) {
		if (!connection.isConnected())
			return null;
		return dataManager.getLocal(locationKey, contentKey);
	}
}
