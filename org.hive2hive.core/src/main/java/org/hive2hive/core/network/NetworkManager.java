package org.hive2hive.core.network;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.security.IH2HEncryption;

public class NetworkManager {

	// TODO this class needs heavy refactoring! many man-in-the-middle delegations and methods that do not
	// belong here

	private final Connection connection;
	private final DataManager dataManager;
	private final MessageManager messageManager;
	private INetworkConfiguration networkConfiguration;
	private H2HSession session;

	private final EventBus eventBus;

	public NetworkManager(IH2HEncryption encryption, EventBus eventBus) {
		this.eventBus = eventBus;

		connection = new Connection(this, encryption);
		dataManager = new DataManager(this, encryption);
		messageManager = new MessageManager(this, encryption);
	}

	/**
	 * Connects to the network based on the provided {@link INetworkConfiguration}s in the constructor.
	 * 
	 * @return <code>true</code> if the connection was successful, <code>false</code> otherwise
	 */
	public boolean connect(INetworkConfiguration networkConfiguration) {
		this.networkConfiguration = networkConfiguration;

		if (networkConfiguration.isLocal()) {
			return connection.connectInternal(networkConfiguration.getNodeID(), networkConfiguration.getBootstapPeer());
		} else {
			boolean success = connection.connect(networkConfiguration.getNodeID());
			// bootstrap if not initial peer
			if (success && !networkConfiguration.isInitialPeer()) {
				success = connection.bootstrap(networkConfiguration.getBootstrapAddress(),
						networkConfiguration.getBootstrapPort());
			}

			return success;
		}

	}

	/**
	 * Disconnects from the network.
	 * 
	 * @return <code>true</code> if the disconnection was successful, <code>false</code> otherwise
	 */
	public boolean disconnect() {
		if (session != null && session.getProfileManager() != null) {
			session.getProfileManager().stopQueueWorker();
		}

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
		return networkConfiguration.getNodeID();
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

	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
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

	public EventBus getEventBus() {
		return eventBus;
	}
}
