package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.LoginProcessContext;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.security.UserCredentials;

public class H2HNode implements IH2HNode, IH2HFileConfiguration {

	private final int maxFileSize;
	private final int maxNumOfVersions;
	private final int maxSizeAllVersions;
	private final int chunkSize;
	private final boolean autostartProcesses;
	private final boolean isMasterPeer;
	private final InetAddress bootstrapAddress;

	private final NetworkManager networkManager;
	private final FileManager fileManager;

	public H2HNode(int maxFileSize, int maxNumOfVersions, int maxSizeAllVersions, int chunkSize,
			boolean autostartProcesses, boolean isMasterPeer, InetAddress bootstrapAddress, String rootPath) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.autostartProcesses = autostartProcesses;
		this.isMasterPeer = isMasterPeer;
		this.bootstrapAddress = bootstrapAddress;

		// TODO set appropriate node ID
		networkManager = new NetworkManager(UUID.randomUUID().toString());
		if (this.isMasterPeer) {
			networkManager.connect();
		} else {
			networkManager.connect(this.bootstrapAddress);
		}

		fileManager = new FileManager(rootPath);
	}

	@Override
	public int getMaxFileSize() {
		return maxFileSize;
	}

	@Override
	public int getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	@Override
	public int getMaxSizeAllVersions() {
		return maxSizeAllVersions;
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	@Override
	public IProcess register(UserCredentials credentials) {

		final RegisterProcess process = new RegisterProcess(credentials, networkManager);

		if (autostartProcesses) {
			process.start();
		}
		return process;
	}

	@Override
	public IProcess login(final UserCredentials credentials) {
		final LoginProcess loginProcess = new LoginProcess(credentials, networkManager);
		loginProcess.addListener(new IProcessListener() {
			@Override
			public void onSuccess() {
				// start the post login process
				LoginProcessContext loginContext = loginProcess.getContext();
				PostLoginProcess postLogin = new PostLoginProcess(loginContext.getGetUserProfileStep()
						.getUserProfile(), credentials, loginContext.getLocations(), networkManager,
						fileManager, H2HNode.this);
				postLogin.start();
			}

			@Override
			public void onFail(String reason) {
				// ignore here
			}
		});

		if (autostartProcesses) {
			loginProcess.start();
		}
		return loginProcess;
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
