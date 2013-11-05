package org.hive2hive.core;

import java.util.UUID;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.register.RegisterProcess;

public class H2HNode implements IH2HNode {

	private final int maxFileSize;
	private final int maxNumOfVersions;
	private final int maxSizeAllVersions;
	private final int chunkSize;
	private final boolean autostartProcesses;
	private final NetworkManager networkManager;

	public H2HNode(int maxFileSize, int maxNumOfVersions, int maxSizeAllVersions, int chunkSize,
			boolean autostartProcesses) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.autostartProcesses = autostartProcesses;

		// TODO initialize the network manager correctly
		networkManager = new NetworkManager(UUID.randomUUID().toString());
		// networkManager.connect();
	}

	public int getMaxFileSize() {
		return maxFileSize;
	}

	public int getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	public int getMaxSizeAllVersions() {
		return maxSizeAllVersions;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	@Override
	public IProcess register(String userId, String password, String pin) {
		RegisterProcess process = new RegisterProcess(userId, password, pin, networkManager);
		if (autostartProcesses) {
			process.start();
		}
		return process;
	}

	@Override
	public IProcess login(String userId, String password, String pin) {
		LoginProcess process = new LoginProcess(userId, password, pin, networkManager);
		if (autostartProcesses) {
			process.start();
		}
		return process;
	}
}
