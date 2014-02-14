package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.api.managers.H2HFileManager;
import org.hive2hive.core.api.managers.H2HUserManager;
import org.hive2hive.core.network.NetworkManager;

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

	public static IH2HNode createNode(INetworkConfiguration networkConfiguration,
			IFileConfiguration fileConfiguration) {
		return new H2HNode(networkConfiguration, fileConfiguration);
	}

	public boolean connect() {
		return networkManager.connect();
	}

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

	// public IH2HNodeStatus getStatus() {
	// boolean connected = networkManager.getConnection().isConnected();
	// int numberOfProcs = ProcessManager.getInstance().getAllProcesses().size();
	// try {
	// H2HSession session = networkManager.getSession();
	// Path root = session.getFileManager().getRoot();
	// String userId = session.getCredentials().getUserId();
	// return new H2HNodeStatus(root.toFile(), userId, connected, numberOfProcs);
	// } catch (NoSessionException e) {
	// return new H2HNodeStatus(null, null, connected, numberOfProcs);
	// }
	// }

}