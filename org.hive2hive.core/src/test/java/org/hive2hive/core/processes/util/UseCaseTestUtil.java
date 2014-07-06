package org.hive2hive.core.processes.util;

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
import org.hive2hive.core.integration.TestFileConfiguration;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.common.GetMetaFileStep;
import org.hive2hive.core.processes.common.GetUserLocationsStep;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IResultProcessComponent;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;

/**
 * Helper class for JUnit tests to get some documents from the DHT.
 * All methods are blocking until the result is here.
 * 
 * @author Nico, Seppi
 * 
 */
public class UseCaseTestUtil {

	private UseCaseTestUtil() {
		// only static methods
	}

	public static void register(UserCredentials credentials, NetworkManager networkManager) throws NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createRegisterProcess(credentials, networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void login(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		UserProfileManager upManager = new UserProfileManager(networkManager.getDataManager(), credentials);
		SessionParameters sessionParameters = new SessionParameters(root.toPath(), upManager, new TestFileConfiguration());
		IProcessComponent process = ProcessFactory.instance().createLoginProcess(credentials, sessionParameters,
				networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void registerAndLogin(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		register(credentials, networkManager);
		login(credentials, networkManager, root);
	}

	public static void registerLoginAndSynchronize(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		registerAndLogin(credentials, networkManager, root);
		synchronize(networkManager);
	}

	public static void logout(NetworkManager networkManager) throws NoPeerConnectionException, NoSessionException {
		ProcessComponent process = ProcessFactory.instance().createLogoutProcess(networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static UserProfile getUserProfile(NetworkManager networkManager, UserCredentials credentials)
			throws GetFailedException, NoPeerConnectionException {
		UserProfileManager manager = new UserProfileManager(networkManager.getDataManager(), credentials);
		return manager.getUserProfile(UUID.randomUUID().toString(), false);
	}

	public static void synchronize(NetworkManager networkManager) {
		ProcessComponent process = ProcessFactory.instance().createSynchronizeFilesProcess(networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void uploadNewFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void uploadNewVersion(NetworkManager networkManager, File file) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static File downloadFile(NetworkManager networkManager, PublicKey fileKey) throws NoSessionException,
			GetFailedException, NoPeerConnectionException {
		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(fileKey, networkManager);
		TestExecutionUtil.executeProcess(process);
		UserProfile userProfile = getUserProfile(networkManager, networkManager.getSession().getCredentials());
		return FileUtil.getPath(networkManager.getSession().getRoot(), userProfile.getFileById(fileKey)).toFile();
	}

	public static void deleteFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void moveFile(NetworkManager networkManager, File source, File destination) throws NoSessionException,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createMoveFileProcess(source, destination, networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static void shareFolder(NetworkManager networkManager, File folder, String friendId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException, NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(friendId, permission), networkManager);
		TestExecutionUtil.executeProcess(process);
	}

	public static MetaFile getMetaFile(NetworkManager networkManager, KeyPair keys) throws NoPeerConnectionException,
			InvalidProcessStateException {
		return getMetaFile(networkManager, keys, true);
	}

	public static MetaFile getMetaFile(NetworkManager networkManager, KeyPair keys, boolean expectSuccess)
			throws NoPeerConnectionException, InvalidProcessStateException {
		GetMetaFileContext context = new GetMetaFileContext(keys);
		GetMetaFileStep step = new GetMetaFileStep(context, networkManager.getDataManager());
		if (expectSuccess) {
			TestExecutionUtil.executeProcess(step);
			return context.metaFile;
		} else {
			TestProcessComponentListener listener = new TestProcessComponentListener();
			step.attachListener(listener);
			step.start();
			TestExecutionUtil.waitTillFailed(listener, TestExecutionUtil.MAX_PROCESS_WAIT_TIME);
			return null;
		}
	}

	public static Locations getLocations(NetworkManager networkManager, String userId) throws NoPeerConnectionException {
		GetUserLocationsContext context = new GetUserLocationsContext(userId);
		GetUserLocationsStep step = new GetUserLocationsStep(context, networkManager.getDataManager());
		TestExecutionUtil.executeProcess(step);
		return context.consumeUserLocations();
	}

	public static List<FileTaste> getFileList(NetworkManager networkManager) throws NoSessionException,
			InvalidProcessStateException {
		IResultProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance().createFileListProcess(
				networkManager);
		TestResultProcessComponentListener<List<FileTaste>> listener = new TestResultProcessComponentListener<List<FileTaste>>();
		fileListProcess.attachListener(listener);
		fileListProcess.start();

		H2HWaiter waiter = new H2HWaiter(TestExecutionUtil.MAX_PROCESS_WAIT_TIME);
		do {
			waiter.tickASecond();
		} while (!listener.hasResultArrived());

		return listener.getResult();
	}
}
