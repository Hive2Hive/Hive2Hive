package org.hive2hive.core.api;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.network.NetworkManager;

/**
 * Default implementation of {@link IH2HNode}.
 * @author Christian, Nico
 *
 */
public class H2HNode implements IH2HNode {

	// TODO atm, this class is just a wrapper for the NetworkManager
	private final INetworkConfiguration networkConfiguration;
	private final IFileConfiguration fileConfiguration;
	private final NetworkManager networkManager;

	private IUserManager userManager;
	private IFileManager fileManager;

	private H2HNode(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguration) {
		this.networkConfiguration = networkConfiguration;
		this.fileConfiguration = fileConfiguration;

		networkManager = new NetworkManager(networkConfiguration);
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
	public static IH2HNode createNode(INetworkConfiguration networkConfiguration,
			IFileConfiguration fileConfiguration) {
		return new H2HNode(networkConfiguration, fileConfiguration);
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
	public IUserManager getUserManager() {
		if (userManager == null)
			userManager = new H2HUserManager(networkManager, fileConfiguration);
		return userManager;
	}

	@Override
	public IFileManager getFileManager() {
		if (fileManager == null)
			fileManager = new H2HFileManager(networkManager);
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
}