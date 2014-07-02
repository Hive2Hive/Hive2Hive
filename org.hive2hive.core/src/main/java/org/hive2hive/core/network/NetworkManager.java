package org.hive2hive.core.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.framework.interfaces.INetworkEventGenerator;
import org.hive2hive.core.events.framework.interfaces.INetworkEventListener;
import org.hive2hive.core.events.implementations.ConnectionEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.security.IH2HEncryption;

public class NetworkManager implements INetworkEventGenerator {

	// TODO this class needs heavy refactoring! many man-in-the-middle delegations and methods that do not
	// belong here

	private final INetworkConfiguration networkConfiguration;

	private final Connection connection;
	private final DataManager dataManager;
	private final MessageManager messageManager;
	private H2HSession session;

	private List<INetworkEventListener> eventListeners;

	public NetworkManager(INetworkConfiguration networkConfiguration, IH2HEncryption encryption) {
		this.networkConfiguration = networkConfiguration;

		connection = new Connection(networkConfiguration.getNodeID(), this, encryption);
		dataManager = new DataManager(this, encryption);
		messageManager = new MessageManager(this, encryption);

		eventListeners = new ArrayList<INetworkEventListener>();
	}

	/**
	 * Connects to the network based on the provided {@link INetworkConfiguration}s in the constructor.
	 * 
	 * @return <code>true</code> if the connection was successful, <code>false</code> otherwise
	 */
	public boolean connect() {
		boolean success = false;
		if (networkConfiguration.isLocal()) {
			if (networkConfiguration.isBootstrappingLocaly()) {
				success = connection.createLocalPeerAndBootstrap(networkConfiguration.getBootstapPeer());
			} else {
				success = connection.createLocalPeer();
			}
		} else if (networkConfiguration.isInitialPeer()) {
			success = connection.connect();
		} else if (networkConfiguration.getBootstrapPort() == -1) {
			success = connection.connect(networkConfiguration.getBootstrapAddress());
		} else {
			success = connection
					.connect(networkConfiguration.getBootstrapAddress(), networkConfiguration.getBootstrapPort());
		}
		notifyConnectionStatus(success);
		return success;
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

		boolean success = connection.disconnect();
		notifyDisconnectionStatus(success);
		return success;
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

	@Override
	public synchronized void addEventListener(INetworkEventListener listener) {
		eventListeners.add(listener);
	}

	@Override
	public synchronized void removeEventListener(INetworkEventListener listener) {
		eventListeners.remove(listener);
	}

	private void notifyConnectionStatus(boolean isSuccessful) {
		Iterator<INetworkEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()) {
			if (isSuccessful) {
				iterator.next().onConnectionSuccess(new ConnectionEvent(networkConfiguration));
			} else {
				iterator.next().onConnectionFailure(new ConnectionEvent(networkConfiguration));
			}
		}
	}

	private void notifyDisconnectionStatus(boolean isSuccessful) {
		Iterator<INetworkEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()) {
			if (isSuccessful) {
				iterator.next().onDisconnectionSuccess(new ConnectionEvent(networkConfiguration));
			} else {
				iterator.next().onDisconnectionFailure(new ConnectionEvent(networkConfiguration));
			}
		}
	}
}
