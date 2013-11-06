package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
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
	private final FileManager fileManager;
	private final boolean isMaster;
	private final InetAddress bootstrapAddress;

	public H2HNode(int maxFileSize, int maxNumOfVersions, int maxSizeAllVersions, int chunkSize,
			boolean autostartProcesses, boolean isMaster, InetAddress bootstrapAddress, String rootPath) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.autostartProcesses = autostartProcesses;
		this.isMaster = isMaster;
		this.bootstrapAddress = bootstrapAddress;

		networkManager = new NetworkManager(UUID.randomUUID().toString());
		if (isMaster) {
			networkManager.connect();
		} else {
			networkManager.connect(bootstrapAddress);
		}

		fileManager = new FileManager(rootPath);
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
	public void disconnect() {
		networkManager.disconnect();
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

	@Override
	public IProcess add(File file) throws IllegalFileLocation {
		// file must be in the given root directory
		if (!file.getAbsolutePath().startsWith(fileManager.getRoot().getAbsolutePath())) {
			throw new IllegalFileLocation();
		}

		return null;
	}
}
