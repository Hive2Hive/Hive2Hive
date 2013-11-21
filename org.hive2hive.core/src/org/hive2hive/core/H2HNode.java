package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.LoginProcessContext;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
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

	private UserCredentials userCredentials;

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

	private UserCredentials getSessionCredentials() throws NoSessionException {
		if (userCredentials == null) {
			throw new NoSessionException();
		}
		return userCredentials;
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
				userCredentials = credentials;
				LoginProcessContext loginContext = loginProcess.getContext();
				startPostLoginProcess(loginContext, credentials);
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

	private void startPostLoginProcess(LoginProcessContext loginContext, UserCredentials credentials) {
		// start the post login process
		PostLoginProcess postLogin = new PostLoginProcess(loginContext.getUserProfile(), credentials,
				loginContext.getLocations(), networkManager, fileManager, H2HNode.this);
		postLogin.start();
	}

	@Override
	public IProcess logout() {
		// TODO start a logout process
		// TODO stop all other processes of this user

		// quit the session
		userCredentials = null;

		// write the current state to a meta file
		fileManager.writePersistentMetaData();
		return null;
	}

	@Override
	public IProcess add(File file) throws IllegalFileLocation, NoSessionException {
		// file must be in the given root directory
		if (!file.getAbsolutePath().startsWith(fileManager.getRoot().getAbsolutePath())) {
			throw new IllegalFileLocation("File must be in root of the H2H directory.");
		}

		// TODO if file is non-empty folder, add all files within the folder (and subfolder)?
		// TODO if file is in folder that does not exist in the network yet --> add parent folder(s) as well?

		NewFileProcess uploadProcess = new NewFileProcess(file, getSessionCredentials(), networkManager,
				fileManager, this);
		if (autostartProcesses) {
			uploadProcess.start();
		}

		return uploadProcess;
	}

	@Override
	public IProcess update(File file) throws NoSessionException {
		NewVersionProcess process = new NewVersionProcess(file, getSessionCredentials(), networkManager,
				fileManager, this);
		if (autostartProcesses) {
			process.start();
		}

		return process;
	}
}
