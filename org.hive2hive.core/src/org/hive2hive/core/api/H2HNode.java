package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;

public class H2HNode extends NetworkNode implements IH2HNode {

	private final ProcessManager processManager; // TODO submit via builder, ProcessManager interface
	private final IUserManager userManager;
	private final IFileManager fileManager;

	private H2HNode(H2HNodeBuilder builder) {
		super(builder.networkConfiguration);
		this.userManager = builder.userManager;
		this.fileManager = builder.fileManager;

		this.processManager = new ProcessManager(true);
	}
	
	@Override
	public void connect() {
		super.connect();
		if (userManager != null)
			userManager.connect();
		if (fileManager != null)
			fileManager.connect();
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		if (userManager != null)
			userManager.disconnect();
		if (fileManager != null)
			fileManager.disconnect();
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

	public static class H2HNodeBuilder {

		// required
		private final INetworkConfiguration networkConfiguration;

		// optional
		private IUserManager userManager;
		private IFileManager fileManager;

		public H2HNodeBuilder(INetworkConfiguration networkConfiguration) {
			this.networkConfiguration = networkConfiguration;
		}

		public H2HNodeBuilder setUserManager(IUserManager userManager) {
			this.userManager = userManager;
			return this;
		}

		public H2HNodeBuilder setFileManager(IFileManager fileManager) {
			this.fileManager = fileManager;
			return this;
		}

		public H2HNode build() {
			return new H2HNode(this);
		}
	}

}
