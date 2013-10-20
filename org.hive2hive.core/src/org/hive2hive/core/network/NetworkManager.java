package org.hive2hive.core.network;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.PeerAddress;

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
	
	public NetworkManager() {
		// TODO give node id
		nodeId = "bla";
		connection = new Connection(nodeId, this);
		messageManager = new MessageManager(this);
		dataManager = new DataManager(this);
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
		return dataManager.getLocal(locationKey, contentKey);
	}
}
