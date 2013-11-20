package org.hive2hive.core.network;

import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;
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

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(NetworkManager.class);

	private final String nodeId;
	private final Connection connection;
	private final MessageManager messageManager;
	private final DataManager dataManager;

	private KeyPair keyPair;

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

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public NetworkManager(String nodeId) {
		this.nodeId = nodeId;
		this.connection = new Connection(nodeId, this);
		this.messageManager = new MessageManager(this);
		this.dataManager = new DataManager(this);
	}

	/**
	 * Create a peer which will be the first node in the network (master).
	 * 
	 * @return <code>true</code> if creating master peer was successful, <code>false</code> if not
	 */
	public boolean connect() {
		return connection.connect();
	}

	/**
	 * Create a peer and bootstrap to a given peer through IP address
	 * 
	 * @param bootstrapInetAddress
	 *            IP address to given bootstrapping peer
	 * @return <code>true</code> if bootstrapping was successful, <code>false</code> if not
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
	 * @return <code>true</code> if bootstrapping was successful, <code>false</code> if not
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
		logger.debug(String.format("Peer '%s' is shut down.", nodeId));
	}

	/**
	 * Sends a given message to the peer which is responsible of the given key. </br>
	 * For sending message directly use {@link MessageManager#sendDirect(BaseDirectMessage)} </br></br>
	 * <b>Important:</b> This message gets encrypted with the node's public key. Use this method for direct
	 * sending to nodes, which have the according private key.
	 * 
	 * @param message
	 *            the message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 * @param listener
	 *            a listener which gets notified about success or failure of sending
	 */
	public void send(BaseMessage message, PublicKey targetPublicKey, IBaseMessageListener listener) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return;
		}
		messageManager.send(message, targetPublicKey, listener);
	}

	/**
	 * Message is sent directly using TCP.</br></br>
	 * <b>Important:</b> This message gets encrypted with the given public key. Use this method for direct
	 * sending to nodes, which have the according private key.
	 * 
	 * @param message
	 *            the message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 * @param listener
	 *            a listener which gets notified about success or failure of sending
	 */
	public void sendDirect(BaseDirectMessage message, PublicKey targetPublicKey, IBaseMessageListener listener) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return;
		}
		messageManager.sendDirect(message, targetPublicKey, listener);
	}

	/**
	 * Stores the content into the DHT at the location under the given content
	 * key
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @param data
	 *            the wrapper containing the content to be stored
	 * @return the future
	 */
	public FuturePut putGlobal(String locationKey, String contentKey, NetworkContent data) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return null;
		}
		return dataManager.putGlobal(locationKey, contentKey, data);
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
	public FutureGet getGlobal(String locationKey, String contentKey) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return null;
		}
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
	 * @param data
	 *            the wrapper containing the content to be stored
	 */
	public void putLocal(String locationKey, String contentKey, NetworkContent data) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return;
		}
		dataManager.putLocal(locationKey, contentKey, data);
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
	public NetworkContent getLocal(String locationKey, String contentKey) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return null;
		}
		return dataManager.getLocal(locationKey, contentKey);
	}

	/**
	 * Removes a content from the DHT
	 * 
	 * @param locationKey the unique id of the content
	 * @param contentKey the content key - please choose one from {@link H2HConstants}
	 * @return the future
	 */
	public FutureRemove remove(String locationKey, String contentKey) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return null;
		}
		return dataManager.remove(locationKey, contentKey);
	}
	
	public FutureRemove remove(String locationKey, String contentKey, Number160 versionKey) {
		if (!connection.isConnected()) {
			logger.warn("Node is not connected!");
			return null;
		}
		return dataManager.remove(locationKey, contentKey, versionKey);
	}
}
