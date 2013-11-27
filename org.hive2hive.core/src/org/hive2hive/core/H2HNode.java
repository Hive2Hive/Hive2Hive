package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.delete.DeleteFileProcess;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.LoginProcessContext;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
import org.hive2hive.core.security.UserCredentials;

public class H2HNode implements IH2HNode, IH2HFileConfiguration {

	private int maxFileSize;
	private int maxNumOfVersions;
	private int maxSizeAllVersions;
	private int chunkSize;
	private boolean autostartProcesses;
	private boolean isMasterPeer;
	private InetAddress bootstrapAddress;
	private String rootPath;

	private final NetworkManager networkManager;

	private H2HSession session;

	/**
	 * Configures an instance of {@link H2HNode}. Use {@link H2HNodeBuilder} to create specific types of instances with specific values.
	 * @param maxFileSize
	 * @param maxNumOfVersions
	 * @param maxSizeAllVersions
	 * @param chunkSize
	 * @param autostartProcesses
	 * @param isMasterPeer
	 * @param bootstrapAddress
	 * @param rootPath
	 */
	public H2HNode(int maxFileSize, int maxNumOfVersions, int maxSizeAllVersions, int chunkSize,
			boolean autostartProcesses, boolean isMasterPeer, InetAddress bootstrapAddress, String rootPath) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.isMasterPeer = isMasterPeer;
		this.autostartProcesses = autostartProcesses;
		this.bootstrapAddress = bootstrapAddress;
		this.rootPath = rootPath;

		// TODO set appropriate node ID
		networkManager = new NetworkManager(UUID.randomUUID().toString());
		if (isMasterPeer) {
			networkManager.connect();
		} else {
			networkManager.connect(bootstrapAddress);
		}
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	private void validateSession() throws NoSessionException {
		if (session == null) {
			throw new NoSessionException();
		}
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
				// create a session
				UserProfileManager profileManager = new UserProfileManager(networkManager, credentials);
				FileManager fileManager = new FileManager(rootPath);

				LoginProcessContext loginContext = loginProcess.getContext();
				session = new H2HSession(loginContext.getUserProfile().getEncryptionKeys(), profileManager,
						H2HNode.this, fileManager);
				networkManager.setSession(session);

				startPostLoginProcess(loginContext.getLocations());
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

	private void startPostLoginProcess(Locations locations) {
		// start the post login process
		PostLoginProcess postLogin = new PostLoginProcess(session, locations, networkManager);
		postLogin.start();
	}

	@Override
	public IProcess logout() throws NoSessionException {
		validateSession();

		// TODO start a logout process
		// TODO stop all other processes of this user

		// write the current state to a meta file
		session.getFileManager().writePersistentMetaData();

		// quit the session
		session = null;
		networkManager.setSession(null);

		return null;
	}

	@Override
	public IProcess add(File file) throws IllegalFileLocation, NoSessionException {
		validateSession();

		// TODO if file is non-empty folder, add all files within the folder (and subfolder)?
		// TODO if file is in folder that does not exist in the network yet --> add parent folder(s) as well?
		NewFileProcess uploadProcess = new NewFileProcess(file, session, networkManager);
		if (autostartProcesses) {
			uploadProcess.start();
		}

		return uploadProcess;
	}

	@Override
	public IProcess update(File file) throws NoSessionException {
		validateSession();

		NewVersionProcess process = new NewVersionProcess(file, session, networkManager);
		if (autostartProcesses) {
			process.start();
		}

		return process;
	}

	@Override
	public IProcess delete(File file) throws IllegalArgumentException, NoSessionException {
		validateSession();

		DeleteFileProcess process = new DeleteFileProcess(file, session, networkManager);

		if (autostartProcesses) {
			process.start();
		}

		return process;
	}

	@Override
	public H2HNode setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
		return this;
	}

	@Override
	public H2HNode setMaxNumOfVersions(int maxNumOfVersions) {
		this.maxNumOfVersions = maxNumOfVersions;
		return this;
	}

	@Override
	public H2HNode setMaxSizeAllVersions(int maxSizeAllVersions) {
		this.maxSizeAllVersions = maxSizeAllVersions;
		return this;
	}

	@Override
	public H2HNode setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		return this;
	}

	@Override
	public H2HNode setAutostartProcesses(boolean autostartProcesses) {
		this.autostartProcesses = autostartProcesses;
		return this;
	}

	@Override
	public H2HNode setMaster(boolean isMasterPeer) {
		this.isMasterPeer = isMasterPeer;
		return this;
	}

	@Override
	public H2HNode setBootstrapAddress(InetAddress bootstrapAddress) {
		this.bootstrapAddress = bootstrapAddress;
		return this;
	}

	@Override
	public H2HNode setRootPath(String rootPath) {
		this.rootPath = rootPath;
		return this;
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
}
