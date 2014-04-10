package org.hive2hive.core.test.processes.util;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.common.GetMetaFileStep;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.files.list.FileTaste;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.junit.Assert;

/**
 * Helper class for JUnit tests to get some documents from the DHT.
 * All methods are blocking until the result is here.
 * 
 * @author Nico, Seppi
 * 
 */
public class UseCaseTestUtil {

	public static final int MAX_PROCESS_WAIT_TIME = 120;

	private UseCaseTestUtil() {
		// only static methods
	}

	public static void waitTillSucceded(TestProcessComponentListener listener, int maxSeconds) {
		H2HWaiter waiter = new H2HWaiter(maxSeconds);
		do {
			if (listener.hasFailed())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	public static void waitTillFailed(TestProcessComponentListener listener, int maxSeconds) {
		H2HWaiter waiter = new H2HWaiter(maxSeconds);
		do {
			if (listener.hasSucceeded())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	/**
	 * Executes a process and waits until it's done. This is a simple helper method to reduce code
	 * clones.
	 */
	public static void executeProcess(IProcessComponent process) {
		executeProcessTillSucceded(process);
	}

	public static void executeProcessTillSucceded(IProcessComponent process) {
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.start();
			waitTillSucceded(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException e) {
			System.out.println("ERROR: Cannot wait until process is done.");
			Assert.fail();
		}
	}

	public static void executeProcessTillFailed(IProcessComponent process) {
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.start();
			waitTillFailed(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException e) {
			System.out.println("ERROR: Cannot wait until process is done.");
		}
	}

	public static void register(UserCredentials credentials, NetworkManager networkManager)
			throws NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createRegisterProcess(credentials,
				networkManager);
		executeProcess(process);
	}

	public static void login(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		SessionParameters sessionParameters = new SessionParameters();
		sessionParameters.setFileConfig(new TestFileConfiguration());
		sessionParameters.setRoot(root.toPath());
		sessionParameters.setProfileManager(new UserProfileManager(networkManager, credentials));
		IProcessComponent process = ProcessFactory.instance().createLoginProcess(credentials,
				sessionParameters, networkManager);
		executeProcess(process);
	}

	public static void registerAndLogin(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		register(credentials, networkManager);
		login(credentials, networkManager, root);
	}

	public static UserProfile getUserProfile(NetworkManager networkManager, UserCredentials credentials)
			throws GetFailedException {
		UserProfileManager manager = new UserProfileManager(networkManager, credentials);
		return manager.getUserProfile(UUID.randomUUID().toString(), false);
	}

	public static void uploadNewFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		executeProcess(process);
	}

	public static void uploadNewVersion(NetworkManager networkManager, File file) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		executeProcess(process);
	}

	public static File downloadFile(NetworkManager networkManager, PublicKey fileKey)
			throws NoSessionException, GetFailedException {
		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(fileKey,
				networkManager);
		executeProcess(process);
		UserProfile userProfile = getUserProfile(networkManager, networkManager.getSession().getCredentials());
		return FileUtil.getPath(networkManager.getSession().getRoot(), userProfile.getFileById(fileKey))
				.toFile();
	}

	public static void deleteFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		executeProcess(process);
	}

	public static void moveFile(NetworkManager networkManager, File source, File destination)
			throws NoSessionException, NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);
		executeProcess(process);
	}

	public static void shareFolder(NetworkManager networkManager, File folder, String friendId,
			PermissionType permission) throws IllegalFileLocation, IllegalArgumentException,
			NoSessionException, NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(friendId, permission), networkManager);
		executeProcess(process);
	}

	public static MetaFile getMetaFile(NetworkManager networkManager, KeyPair keys)
			throws NoPeerConnectionException, InvalidProcessStateException {
		return getMetaFile(networkManager, keys, true);
	}

	public static MetaFile getMetaFile(NetworkManager networkManager, KeyPair keys, boolean expectSuccess)
			throws NoPeerConnectionException, InvalidProcessStateException {
		GetMetaFileContext context = new GetMetaFileContext(keys);
		GetMetaFileStep step = new GetMetaFileStep(context, context, networkManager.getDataManager());
		if (expectSuccess) {
			executeProcess(step);
			return context.metaFile;
		} else {
			TestProcessComponentListener listener = new TestProcessComponentListener();
			step.attachListener(listener);
			step.start();
			waitTillFailed(listener, MAX_PROCESS_WAIT_TIME);
			return null;
		}
	}

	public static Locations getLocations(NetworkManager networkManager, String userId)
			throws NoPeerConnectionException {
		GetUserLocationsContext context = new GetUserLocationsContext();
		GetUserLocationsStep step = new GetUserLocationsStep(userId, context, networkManager.getDataManager());
		executeProcess(step);
		return context.locations;
	}

	public static List<FileTaste> getFileList(NetworkManager networkManager) throws NoSessionException,
			InvalidProcessStateException {
		IResultProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance()
				.createFileListProcess(networkManager);
		TestResultProcessComponentListener<List<FileTaste>> listener = new TestResultProcessComponentListener<List<FileTaste>>();
		fileListProcess.attachListener(listener);
		fileListProcess.start();

		H2HWaiter waiter = new H2HWaiter(MAX_PROCESS_WAIT_TIME);
		do {
			waiter.tickASecond();
		} while (!listener.hasResultArrived());

		return listener.getResult();
	}
}
