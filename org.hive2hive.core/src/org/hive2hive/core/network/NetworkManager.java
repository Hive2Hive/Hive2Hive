package org.hive2hive.core.network;

import java.security.PrivateKey;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.MessageManager;

public class NetworkManager {

	// TODO this class needs heavy refactoring! many man-in-the-middle delegations and methods that do not
	// belong here

	private final INetworkConfiguration networkConfiguration;

	private final Connection connection;
	private final DataManager dataManager;
	private final MessageManager messageManager;

	private H2HSession session;
	private PublicKeyManager keyManager;

	public NetworkManager(INetworkConfiguration networkConfiguration) {
		this.networkConfiguration = networkConfiguration;

		connection = new Connection(networkConfiguration.getNodeID(), this);
		dataManager = new DataManager(this);
		messageManager = new MessageManager(this);
	}

	/**
	 * Connects to the network based on the provided {@link INetworkConfiguration}s in the constructor.
	 * 
	 * @return True, if the connection was successful, false otherwise.
	 */
	public boolean connect() {
		if (networkConfiguration.isMasterPeer()) {
			return connection.connect();
		} else if (networkConfiguration.getBootstrapPort() == -1) {
			return connection.connect(networkConfiguration.getBootstrapAddress());
		} else {
			return connection.connect(networkConfiguration.getBootstrapAddress(),
					networkConfiguration.getBootstrapPort());
		}
	}

	/**
	 * Disconnects from the network.
	 * @return True, if the disconnection was successful, false otherwise.
	 */
	public boolean disconnect() {

		if (session != null && session.getProfileManager() != null)
			session.getProfileManager().stopQueueWorker();

		return connection.disconnect();
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
		if (session == null)
			throw new NoSessionException();
		return session;
	}

	/**
	 * Helper method that returns the public key of the currently logged in user.
	 */
	public PublicKey getPublicKey() {
		try {
			return getPublicKey(getUserId());
		} catch (GetFailedException e) {
			return null;
		}
	}

	/**
	 * Get the public key of the given user. The call may block.
	 * 
	 * @param userId the unique id of the user
	 * @return a public key
	 * @throws GetFailedException if a failure occurs or no public key found
	 */
	public PublicKey getPublicKey(String userId) throws GetFailedException {
		if (session == null)
			return null;
		createKeyManager();
		return keyManager.getPublicKey(userId);
	}

	/**
	 * Helper method that returns the private key of the currently logged in user
	 */
	public PrivateKey getPrivateKey() {
		if (session == null)
			return null;
		createKeyManager();
		return keyManager.getUsersPrivateKey();
	}

	public String getUserId() {
		if (session == null)
			return null;
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

	private void createKeyManager() {
		if (keyManager == null)
			keyManager = new PublicKeyManager(session.getCredentials().getUserId(), session.getKeyPair(),
					dataManager);
	}

}
