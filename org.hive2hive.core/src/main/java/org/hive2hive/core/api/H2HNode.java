package org.hive2hive.core.api;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.framework.interfaces.INetworkEventListener;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.IH2HEncryption;

/**
 * Default implementation of {@link IH2HNode}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HNode implements IH2HNode {

	// TODO remove manager singletons
	// TODO atm, this class is just a wrapper for the NetworkManager
	private final INetworkConfiguration networkConfiguration;
	private final IFileConfiguration fileConfiguration;
	private final NetworkManager networkManager;

	private IUserManager userManager;
	private IFileManager fileManager;

	private H2HNode(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguration,
			IH2HEncryption encryption) {
		this.networkConfiguration = networkConfiguration;
		this.fileConfiguration = fileConfiguration;

		networkManager = new NetworkManager(networkConfiguration, encryption);
	}

	/**
	 * Create a Hive2Hive node instance. Before the node can be used, a {@link IH2HNode#connect()} must be
	 * called.
	 * 
	 * @param networkConfiguration the network parameters, important to know how to bootstrap and which port
	 *            to listen to.
	 * @param fileConfiguration the file configuration
	 * @return
	 */
	public static IH2HNode createNode(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguration) {
		return new H2HNode(networkConfiguration, fileConfiguration, new H2HDefaultEncryption());
	}

	/**
	 * Same as {@link H2HNode#createNode(INetworkConfiguration, IFileConfiguration)}, but with additional
	 * capability to provide an own encryption implementation
	 * 
	 * @param networkConfiguration the network parameters, important to know how to bootstrap and which port
	 *            to listen to.
	 * @param fileConfiguration the file configuration
	 * @param encryption and decryption implementation
	 * @return
	 */
	public static IH2HNode createNode(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguration,
			IH2HEncryption encryption) {
		return new H2HNode(networkConfiguration, fileConfiguration, encryption);
	}

	@Override
	public boolean connect() {
		return networkManager.connect();
	}

	@Override
	public boolean disconnect() {
		return networkManager.disconnect();
	}

	@Override
	public boolean isConnected() {
		return networkManager.isConnected();
	}

	@Override
	public IUserManager getUserManager() {
		if (userManager == null) {
			userManager = new H2HUserManager(networkManager, fileConfiguration);
		}
		return userManager;
	}

	@Override
	public IFileManager getFileManager() {
		if (fileManager == null) {
			fileManager = new H2HFileManager(networkManager);
		}
		return fileManager;
	}

	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	@Override
	public Peer getPeer() {
		return networkManager.getConnection().getPeer();
	}

	@Override
	public synchronized void addEventListener(INetworkEventListener listener) {
		networkManager.addEventListener(listener);
	}

	@Override
	public synchronized void removeEventListener(INetworkEventListener listener) {
		networkManager.removeEventListener(listener);
	}
}
