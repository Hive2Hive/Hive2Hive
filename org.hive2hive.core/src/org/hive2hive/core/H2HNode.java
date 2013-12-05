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

	private boolean autostartProcesses;
	private final int maxSizeOfAllVersions;
	private final int maxFileSize;
	private final int maxNumOfVersions;
	private final int chunkSize;

	private final NetworkManager networkManager;

	/**
	 * Configures an instance of {@link H2HNode}. Use {@link H2HNodeBuilder} to create specific types of
	 * instances with specific values.
	 * 
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
			boolean autostartProcesses, boolean isMasterPeer, InetAddress bootstrapAddress) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeOfAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.autostartProcesses = autostartProcesses;

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

	@Override
	public IProcess register(UserCredentials credentials) {
		final RegisterProcess process = new RegisterProcess(credentials, networkManager);

		if (autostartProcesses) {
			process.start();
		}
		return process;
	}

	@Override
	public IProcess login(final UserCredentials credentials, final File rootPath) {
		final LoginProcess loginProcess = new LoginProcess(credentials, networkManager);

		// TODO this makes no sense actually, since the IProcess is returned...
		loginProcess.addListener(new IProcessListener() {
			@Override
			public void onSuccess() {
				// create a session
				UserProfileManager profileManager = new UserProfileManager(networkManager, credentials);
				FileManager fileManager = new FileManager(rootPath);

				LoginProcessContext loginContext = loginProcess.getContext();
				H2HSession session = new H2HSession(loginContext.getUserProfile().getEncryptionKeys(),
						profileManager, H2HNode.this, fileManager);
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
		try {
			// start the post login process
			PostLoginProcess postLogin = new PostLoginProcess(locations, networkManager);
			postLogin.start();
		} catch (NoSessionException e) {
			// TODO handle the exception
		}
	}

	@Override
	public IProcess logout() throws NoSessionException {
		// TODO start a logout process
		// TODO stop all other processes of this user as soon as the logout process is done

		// write the current state to a meta file
		networkManager.getSession().getFileManager().writePersistentMetaData();

		// quit the session
		networkManager.setSession(null);

		return null;
	}

	@Override
	public IProcess add(File file) throws IllegalFileLocation, NoSessionException {
		// TODO if file is non-empty folder, add all files within the folder (and subfolder)?
		// TODO if file is in folder that does not exist in the network yet --> add parent folder(s) as well?
		NewFileProcess uploadProcess = new NewFileProcess(file, networkManager);
		if (autostartProcesses) {
			uploadProcess.start();
		}

		return uploadProcess;
	}

	@Override
	public IProcess update(File file) throws NoSessionException {
		NewVersionProcess process = new NewVersionProcess(file, networkManager);
		if (autostartProcesses) {
			process.start();
		}

		return process;
	}

	@Override
	public IProcess delete(File file) throws IllegalArgumentException, NoSessionException {
		DeleteFileProcess process = new DeleteFileProcess(file, networkManager);

		if (autostartProcesses) {
			process.start();
		}

		return process;
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
		return maxSizeOfAllVersions;
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}
}
