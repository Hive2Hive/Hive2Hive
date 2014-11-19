package org.hive2hive.core.utils;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.integration.TestFileConfiguration;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.GetMetaFileStep;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.helper.GetMetaFileContext;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.Assert;

/**
 * Helper class for JUnit tests to get some documents from the DHT.
 * All methods are blocking until the result is here.
 * 
 * @author Nico, Seppi
 */
public class UseCaseTestUtil {

	private UseCaseTestUtil() {
		// only static methods
	}

	public static void register(UserCredentials credentials, NetworkManager networkManager) throws NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createRegisterProcess(credentials, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void login(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		login(credentials, networkManager, new TestFileAgent(root));
	}

	public static void login(UserCredentials credentials, NetworkManager networkManager, IFileAgent fileAgent)
			throws NoPeerConnectionException {
		SessionParameters sessionParameters = new SessionParameters(fileAgent, new TestFileConfiguration());
		IProcessComponent<Void> process = ProcessFactory.instance().createLoginProcess(credentials, sessionParameters,
				networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void registerAndLogin(UserCredentials credentials, NetworkManager networkManager, File root)
			throws NoPeerConnectionException {
		register(credentials, networkManager);
		login(credentials, networkManager, root);
	}

	public static void logout(NetworkManager networkManager) throws NoPeerConnectionException, NoSessionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createLogoutProcess(networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static UserProfile getUserProfile(NetworkManager networkManager, UserCredentials credentials)
			throws GetFailedException, NoPeerConnectionException {
		UserProfileManager manager = new UserProfileManager(networkManager.getDataManager(), credentials);
		return manager.readUserProfile();
	}

	public static void uploadNewFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createAddFileProcess(file, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void uploadNewVersion(NetworkManager networkManager, File file) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static File downloadFile(NetworkManager networkManager, PublicKey fileKey) throws NoSessionException,
			GetFailedException, NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createDownloadFileProcess(fileKey, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
		UserProfile userProfile = getUserProfile(networkManager, networkManager.getSession().getCredentials());
		return userProfile.getFileById(fileKey).asFile(networkManager.getSession().getRootFile());
	}

	public static void downloadFile(NetworkManager networkManager, File file) throws NoSessionException, GetFailedException,
			NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createDownloadFileProcess(file, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void deleteFile(NetworkManager networkManager, File file) throws NoSessionException,
			NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void moveFile(NetworkManager networkManager, File source, File destination) throws NoSessionException,
			NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static void shareFolder(NetworkManager networkManager, File folder, String friendId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException, NoPeerConnectionException {
		IProcessComponent<Void> process = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(friendId, permission), networkManager);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	public static BaseMetaFile getMetaFile(NetworkManager networkManager, KeyPair keys) throws NoPeerConnectionException,
			InvalidProcessStateException, ProcessExecutionException {
		return getMetaFile(networkManager, keys, true);
	}

	public static BaseMetaFile getMetaFile(NetworkManager networkManager, KeyPair keys, boolean expectSuccess)
			throws NoPeerConnectionException {
		GetMetaFileContext context = new GetMetaFileContext(keys);
		GetMetaFileStep step = new GetMetaFileStep(context, networkManager.getDataManager());
		if (expectSuccess) {
			TestExecutionUtil.executeProcessTillSucceded(step);
			return context.metaFile;
		} else {
			try {
				step.execute();
				Assert.fail("Expected the process to fail");
			} catch (InvalidProcessStateException | ProcessExecutionException e) {
				// expected
			}
			return null;
		}
	}

	public static List<FileTaste> getFileList(NetworkManager networkManager) throws NoSessionException,
			InvalidProcessStateException, ProcessExecutionException {
		IProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance().createFileListProcess(networkManager);

		return fileListProcess.execute();
	}
}
