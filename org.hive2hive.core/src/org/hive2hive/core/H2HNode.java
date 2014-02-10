package org.hive2hive.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.ProcessManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil.FileProcessAction;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
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
	private final long maxSizeOfAllVersions;
	private final long maxFileSize;
	private final long maxNumOfVersions;
	private final long chunkSize;

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
	public H2HNode(long maxFileSize, long maxNumOfVersions, long maxSizeAllVersions, long chunkSize,
			boolean autostartProcesses, boolean isMasterPeer, InetAddress bootstrapAddress) {
		assert maxFileSize > 0;
		this.maxFileSize = maxFileSize;

		assert maxNumOfVersions > 0;
		this.maxNumOfVersions = maxNumOfVersions;

		assert maxSizeAllVersions > 0;
		this.maxSizeOfAllVersions = maxSizeAllVersions;

		assert chunkSize > 0;
		this.chunkSize = chunkSize;

		this.autostartProcesses = autostartProcesses;

		networkManager = new NetworkManager(UUID.randomUUID().toString());
		if (isMasterPeer) {
			networkManager.connect();
		} else {
			networkManager.connect(bootstrapAddress);
		}
	}

	private void autoStartProcess(IProcessComponent process) {
		try {
			if (autostartProcesses)
				process.start();
		} catch (InvalidProcessStateException e) {
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
	public IH2HNodeStatus getStatus() {
		boolean connected = networkManager.getConnection().isConnected();
		int numberOfProcs = ProcessManager.getInstance().getAllProcesses().size();
		try {
			H2HSession session = networkManager.getSession();
			Path root = session.getFileManager().getRoot();
			String userId = session.getCredentials().getUserId();
			return new H2HNodeStatus(root.toFile(), userId, connected, numberOfProcs);
		} catch (NoSessionException e) {
			return new H2HNodeStatus(null, null, connected, numberOfProcs);
		}
	}

	@Override
	public void disconnect() {
		networkManager.disconnect();
	}

	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException {
		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials,
				networkManager);
		autoStartProcess(registerProcess);
		return registerProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, Path rootPath)
			throws NoPeerConnectionException {
		SessionParameters sessionParameters = new SessionParameters();
		sessionParameters.setProfileManager(new UserProfileManager(networkManager, credentials));
		sessionParameters.setFileManager(new FileManager(rootPath));
		sessionParameters.setFileConfig(H2HNode.this);

		ProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials,
				sessionParameters, networkManager);
		autoStartProcess(loginProcess);
		return loginProcess;
	}

	@Override
	public IProcessComponent logout() throws NoSessionException, NoPeerConnectionException {
		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(networkManager);
		logoutProcess.attachListener(new IProcessComponentListener() {

			private boolean done = false;

			@Override
			public void onSucceeded() {
				onFinished();
			}

			@Override
			public void onFinished() {
				if (!done) {
					done = true;
					postLogoutWork();
				}

			}

			@Override
			public void onFailed(RollbackReason reason) {
				onFinished();
			}
		});

		autoStartProcess(logoutProcess);
		return logoutProcess;
	}

	private void postLogoutWork() {
		// stop all running processes
		ProcessManager.getInstance().stopAll("Logout stopped all processes.");

		// quit the session
		networkManager.setSession(null);
	}

	@Override
	public IProcessComponent add(File file) throws IllegalFileLocation, NoSessionException,
			NoPeerConnectionException {
		IProcessComponent process;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			process = FileRecursionUtil.buildUploadProcess(preorderList, FileProcessAction.NEW_FILE,
					networkManager);
		} else {
			// add single file
			process = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		}

		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcessComponent move(File source, File destination) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);
		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcessComponent delete(File file) throws IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		IProcessComponent process;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			process = FileRecursionUtil.buildDeletionProcess(preorderList, networkManager);
		} else {
			// delete a single file
			process = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		}

		autoStartProcess(process);
		return process;
	}

	@Override
	public IResultProcessComponent<List<Path>> getFileList() throws NoSessionException {
		IResultProcessComponent<List<Path>> process = ProcessFactory.instance().createFileListProcess(
				networkManager);
		autoStartProcess(process);
		return process;
	}

	@Override
	public IProcessComponent recover(File file, IVersionSelector versionSelector) throws NoSessionException,
			FileNotFoundException, IllegalArgumentException, NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createRecoverFileProcess(file, versionSelector,
				networkManager);
		autoStartProcess(process);
		return process;
	}

	@Override
	public long getMaxFileSize() {
		return maxFileSize;
	}

	@Override
	public long getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	@Override
	public long getMaxSizeAllVersions() {
		return maxSizeOfAllVersions;
	}

	@Override
	public long getChunkSize() {
		return chunkSize;
	}

	@Override
	public IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalArgumentException, NoSessionException, IllegalFileLocation,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(userId, permission), networkManager);
		autoStartProcess(process);
		return process;
	}
}
