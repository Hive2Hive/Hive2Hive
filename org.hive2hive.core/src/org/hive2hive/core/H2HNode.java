package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.delete.DeleteFileProcess;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.LoginProcessContext;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.logout.LogoutProcess;
import org.hive2hive.core.process.move.MoveFileProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
import org.hive2hive.core.process.util.FileRecursionUtil;
import org.hive2hive.core.process.util.FileRecursionUtil.FileProcessAction;
import org.hive2hive.core.security.UserCredentials;

/**
 * This is the central class for a developer using the Hive2Hive library. A node represents a peer in the
 * network, forming a distributed hash table (DHT). Each node needs to create a new network or connect to an
 * existing network (bootstrapping).<br>
 * To create a new peer, use the {@link H2HNodeBuilder} to configure the node and set it up (it helps to fill
 * this awful constructor). A instance of this class opens the world for user and file management.
 * 
 * @author Nico, Chris, Seppi
 * 
 */
public class H2HNode implements IH2HNode, IFileConfiguration, IFileManagement, IUserManagement {

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

	private void autoStartProcess(IProcess process) {
		try {
			if (autostartProcesses)
				process.start();
		} catch (IllegalProcessStateException e) {
			// ignore
		}
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return this;
	}

	@Override
	public IUserManagement getUserManagement() {
		return this;
	}

	@Override
	public IFileManagement getFileManagement() {
		return this;
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	@Override
	public IProcess register(UserCredentials credentials) {
		final RegisterProcess process = new RegisterProcess(credentials, networkManager);

		autoStartProcess(process);
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
			public void onFail(Exception error) {
				// ignore here
			}
		});

		autoStartProcess(loginProcess);
		return loginProcess;
	}

	private void startPostLoginProcess(Locations locations) {
		try {
			// start the post login process
			PostLoginProcess postLogin = new PostLoginProcess(locations, networkManager);
			postLogin.start();
		} catch (Exception e) {
			// TODO handle the exception
		}
	}

	@Override
	public IProcess logout() throws NoSessionException {
		LogoutProcess logoutProcess = new LogoutProcess(networkManager);
		logoutProcess.addListener(new IProcessListener() {
			@Override
			public void onSuccess() {
				postLogoutWork();
			}

			@Override
			public void onFail(Exception error) {
				postLogoutWork();
			}
		});

		autoStartProcess(logoutProcess);
		return logoutProcess;
	}

	private void postLogoutWork() {
		// stop all running processes
		ProcessManager.getInstance().stopAll("Logout stopped all processes.");

		// write the current state to a meta file
		try {
			networkManager.getSession().getFileManager().writePersistentMetaData();

			// quit the session
			networkManager.setSession(null);
		} catch (NoSessionException e) {
			// ignore
		}
	}

	@Override
	public IProcess add(File file) throws IllegalFileLocation, NoSessionException {
		IProcess process;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<File> preorderList = FileRecursionUtil.getPreorderList(file);
			process = FileRecursionUtil.buildProcessTree(preorderList, networkManager,
					FileProcessAction.NEW_FILE);
		} else {
			// add single file
			process = new NewFileProcess(file, networkManager);
		}

		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcess update(File file) throws NoSessionException, IllegalArgumentException {
		NewVersionProcess process = new NewVersionProcess(file, networkManager);
		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcess move(File source, File destination) throws NoSessionException, IllegalArgumentException {
		MoveFileProcess process = new MoveFileProcess(networkManager, source, destination);
		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcess delete(File file) throws IllegalArgumentException, NoSessionException {
		IProcess process;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<File> preorderList = FileRecursionUtil.getPreorderList(file);
			process = FileRecursionUtil.buildProcessTree(preorderList, networkManager,
					FileProcessAction.DELETE);
		} else {
			// delete a single file
			process = new DeleteFileProcess(file, networkManager);
		}

		autoStartProcess(process);
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
