package org.hive2hive.core.network;

import net.tomp2p.dht.PeerDHT;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.serializer.IH2HSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager {

	private static final Logger logger = LoggerFactory.getLogger(NetworkManager.class);

	private final IH2HEncryption encryption;
	private final Connection connection;
	private final DataManager dataManager;
	private final MessageManager messageManager;
	private String nodeID;
	private H2HSession session;

	private EventBus eventBus;
	private final DownloadManager downloadManager;

	public NetworkManager(IH2HEncryption encryption, IH2HSerialize serializer, IFileConfiguration fileConfig) {
		this.encryption = encryption;
		connection = new Connection(this, serializer);
		dataManager = new DataManager(connection, serializer, encryption);
		messageManager = new MessageManager(this, serializer);
		downloadManager = new DownloadManager(this, fileConfig);
	}

	/**
	 * Connects to the network based on the provided {@link INetworkConfiguration}s in the constructor.
	 * 
	 * @return <code>true</code> if the connection was successful, <code>false</code> otherwise
	 */
	public boolean connect(INetworkConfiguration networkConfiguration) {
		this.eventBus = new EventBus();
		this.nodeID = networkConfiguration.getNodeID();
		return connection.connect(networkConfiguration);
	}

	/**
	 * Uses an existing peer for DHT interaction
	 */
	public boolean connect(PeerDHT peer, boolean startReplication) {
		this.eventBus = new EventBus();
		this.nodeID = peer.peerID().toString();
		return connection.connect(peer, startReplication);
	}

	/**
	 * Disconnects from the network.
	 * 
	 * @param keepSession <code>false</code> if the session should also be wiped.
	 * 
	 * @return <code>true</code> if the disconnection was successful, <code>false</code> otherwise
	 */
	public boolean disconnect(boolean keepSession) {
		if (session != null && !keepSession) {
			if (session.getProfileManager() != null) {
				session.getProfileManager().stopQueueWorker();
			}
			if (session.getDownloadManager() != null) {
				session.getDownloadManager().stopBackgroundProcesses();
			}
		}

		eventBus.shutdown();
		logger.debug("Eventbus stopped");

		return connection.disconnect();
	}

	/**
	 * Checks whether this {@link NetworkManager} is connected to a network.
	 * 
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 */
	public boolean isConnected() {
		return connection.isConnected();
	}

	public String getNodeId() {
		return nodeID;
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets the session of the logged in user in order to receive messages.
	 */
	public void setSession(H2HSession session) {
		this.session = session;
	}

	/**
	 * Returns the session of the currently logged in user.
	 */
	public H2HSession getSession() throws NoSessionException {
		if (session == null) {
			throw new NoSessionException();
		}
		return session;
	}

	/**
	 * Convenience method to get the user id of the currently logged in user
	 * 
	 * @return the user id or null in case no session exists
	 */
	public String getUserId() {
		if (session == null) {
			return null;
		}
		return session.getCredentials().getUserId();
	}

	public DataManager getDataManager() throws NoPeerConnectionException {
		if (!connection.isConnected() || dataManager == null) {
			throw new NoPeerConnectionException();
		}
		return dataManager;
	}

	public MessageManager getMessageManager() throws NoPeerConnectionException {
		if (!connection.isConnected() || messageManager == null) {
			throw new NoPeerConnectionException();
		}
		return messageManager;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public EventBus getEventBus() {
		if (eventBus == null) {
			throw new IllegalStateException("No EventBus instance provided.");
		}

		return eventBus;
	}

	public IH2HEncryption getEncryption() {
		return encryption;
	}
}
