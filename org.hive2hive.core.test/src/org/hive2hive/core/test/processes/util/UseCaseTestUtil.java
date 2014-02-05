package org.hive2hive.core.test.processes.util;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.UUID;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.common.GetMetaDocumentStep;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
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
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.start();
			waitTillSucceded(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException e) {
			System.out.println("ERROR: Cannot wait until process is done");
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
		sessionParameters.setFileManager(new FileManager(root.toPath()));
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

	// public static void putUserProfile(NetworkManager networkManager, UserProfile profile,
	// UserCredentials credentials) throws NoPeerConnectionException {
	// executeProcess(new PutUserProfileStep(credentials, profile, networkManager.getDataManager()));
	// }

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
		return networkManager.getSession().getFileManager().getPath(userProfile.getFileById(fileKey))
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

	public static void shareFolder(NetworkManager networkManager, File folder, String friendId)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		ProcessComponent process = ProcessFactory.instance().createShareProcess(folder, friendId,
				networkManager);
		executeProcess(process);
	}

	public static MetaDocument getMetaDocument(NetworkManager networkManager, final KeyPair keys)
			throws NoPeerConnectionException {
		GetMetaDocumentContext context = new GetMetaDocumentContext(keys);
		GetMetaDocumentStep step = new GetMetaDocumentStep(context, context, networkManager.getDataManager());
		executeProcess(step);
		return context.metaDocument;
	}

	public static Locations getLocations(NetworkManager networkManager, String userId)
			throws NoPeerConnectionException {
		GetUserLocationsContext context = new GetUserLocationsContext();
		GetUserLocationsStep step = new GetUserLocationsStep(userId, context, networkManager.getDataManager());
		executeProcess(step);
		return context.locations;
	}

	// public static IGetFileListProcess getDigest(NetworkManager networkManager,
	// UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config) {
	//
	// networkManager.setSession(new H2HSession(EncryptionUtil
	// .generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));
	// GetFileListProcess process = null;
	// try {
	// process = new GetFileListProcess(networkManager);
	// executeProcess(process);
	// } catch (NoSessionException e) {
	// // never happens because session is set before
	// }
	//
	// return process;
	// }
}
