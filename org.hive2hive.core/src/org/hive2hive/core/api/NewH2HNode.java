package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.INewH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.network.NetworkManager;

public class NewH2HNode implements INewH2HNode {

	private final INetworkConfiguration networkConfiguration;
	private final NetworkManager networkManager;
	private final ProcessManager processManager; // TODO submit via builder, ProcessManager interface
	private final IUserManager userManager;
	private final IFileManager fileManager;
	private final IFileConfiguration fileConfiguration;

	private NewH2HNode(H2HNodeBuilder builder) {
		this.networkConfiguration = builder.networkConfiguration;
		this.userManager = builder.userManager;
		this.fileManager = builder.fileManager;
		this.fileConfiguration = builder.fileConfiguration;

		this.networkManager = new NetworkManager(networkConfiguration.getNodeID());
		this.processManager = new ProcessManager(true);
	}

	@Override
	public void connect() {
		if (networkConfiguration.isMasterPeer()) {
			networkManager.connect();
		} else {
			networkManager.connect(networkConfiguration.getBootstrapAddress());
		}
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}

	@Override
	public ProcessManager getProcessManager() {
		return processManager;
	}

	@Override
	public IUserManager getUserManager() {
		return userManager;
	}

	@Override
	public IFileManager getFileManager() {
		return fileManager;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	public static class H2HNodeBuilder {

		// required
		private final INetworkConfiguration networkConfiguration;

		// optional
		private IUserManager userManager;
		private IFileManager fileManager;
		private IFileConfiguration fileConfiguration;

		public H2HNodeBuilder(INetworkConfiguration networkConfiguration) {
			this.networkConfiguration = networkConfiguration;
		}

		public H2HNodeBuilder setUserManager(IUserManager userManager) {
			this.userManager = userManager;
			return this;
		}

		public H2HNodeBuilder setFileManager(IFileManager fileManager, IFileConfiguration fileConfiguration) {
			this.fileManager = fileManager;
			this.fileConfiguration = fileConfiguration;
			return this;
		}

		public NewH2HNode build() {
			return new NewH2HNode(this);
		}
	}

}
