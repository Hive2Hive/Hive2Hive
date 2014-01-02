package org.hive2hive.core.test.process;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.delete.DeleteFileProcess;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.login.GetUserProfileStep;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.core.process.move.MoveFileProcess;
import org.hive2hive.core.process.register.PutUserProfileStep;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.junit.Assert;

/**
 * Helper class for JUnit tests to get some documents from the DHT.
 * All methods are blocking until the result is here.
 * 
 * @author Nico
 * 
 */
public class ProcessTestUtil {

	private ProcessTestUtil() {
		// only static methods
	}

	/**
	 * Executes a process step and waits until it's done. This is a simple helper method to reduce code
	 * clones.
	 */
	private static void executeStep(NetworkManager networkManager, ProcessStep toExecute) {
		Process process = new Process(networkManager) {
		};
		process.setNextStep(toExecute);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(30);
		do {
			if (listener.hasFailed())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	/**
	 * Executes a process and waits until it's done. This is a simple helper method to reduce code
	 * clones.
	 */
	private static void executeProcess(Process process) {
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(60);
		do {
			if (listener.hasFailed())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	public static UserProfile register(UserCredentials credentials, NetworkManager networkManager) {
		RegisterProcess register = new RegisterProcess(credentials, networkManager);
		executeProcess(register);
		return register.getContext().getUserProfile();
	}

	public static UserProfile login(UserCredentials credentials, NetworkManager networkManager, File root) {
		SessionParameters sessionParameters = new SessionParameters();
		sessionParameters.setFileConfig(new TestFileConfiguration());
		sessionParameters.setFileManager(new FileManager(root.toPath()));
		sessionParameters.setProfileManager(new UserProfileManager(networkManager, credentials));
		LoginProcess login = new LoginProcess(credentials, sessionParameters, networkManager);
		executeProcess(login);

		return login.getContext().getUserProfile();
	}

	public static UserProfile getUserProfile(NetworkManager networkManager, UserCredentials credentials) {
		IGetUserProfileContext context = new IGetUserProfileContext() {

			private UserProfile userProfile;

			@Override
			public void setUserProfile(UserProfile userProfile) {
				this.userProfile = userProfile;
			}

			@Override
			public UserProfile getUserProfile() {
				return userProfile;
			}
		};

		GetUserProfileStep step = new GetUserProfileStep(credentials, context, null);
		executeStep(networkManager, step);
		return context.getUserProfile();
	}

	public static void putUserProfile(NetworkManager networkManager, UserProfile profile,
			UserCredentials credentials) {
		PutUserProfileStep step = new PutUserProfileStep(profile, credentials, null);
		executeStep(networkManager, step);
	}

	public static MetaDocument getMetaDocument(NetworkManager networkManager, KeyPair keys) {
		IGetMetaContext context = new IGetMetaContext() {

			private MetaDocument metaDocument;

			@Override
			public void setMetaDocument(MetaDocument metaDocument) {
				this.metaDocument = metaDocument;
			}

			@Override
			public MetaDocument getMetaDocument() {
				return metaDocument;
			}
		};

		GetMetaDocumentStep step = new GetMetaDocumentStep(keys, null, context);
		executeStep(networkManager, step);
		return context.getMetaDocument();
	}

	public static Locations getLocations(NetworkManager networkManager, String userId) {
		IGetLocationsContext context = new IGetLocationsContext() {

			private Locations locations;

			@Override
			public void setLocations(Locations locations) {
				this.locations = locations;
			}

			@Override
			public Locations getLocations() {
				return locations;
			}
		};

		GetLocationsStep step = new GetLocationsStep(userId, null, context);
		executeStep(networkManager, step);
		return context.getLocations();
	}

	public static File downloadFile(NetworkManager networkManager, FileTreeNode file,
			UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config) {
		networkManager.setSession(new H2HSession(EncryptionUtil
				.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));
		try {
			DownloadFileProcess process = new DownloadFileProcess(file, networkManager);
			executeProcess(process);
		} catch (NoSessionException e) {
			// never happens because session is set before
		}

		return fileManager.getPath(file).toFile();
	}

	public static void uploadNewFile(NetworkManager networkManager, File file,
			UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config)
			throws IllegalFileLocation {
		networkManager.setSession(new H2HSession(EncryptionUtil
				.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));
		try {
			NewFileProcess process = new NewFileProcess(file, networkManager);
			executeProcess(process);
		} catch (NoSessionException e) {
			// never happens because session is set before
		}
	}

	public static void uploadNewFileVersion(NetworkManager networkManager, File file,
			UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config)
			throws IllegalArgumentException {
		networkManager.setSession(new H2HSession(EncryptionUtil
				.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));

		try {
			NewVersionProcess process = new NewVersionProcess(file, networkManager);
			executeProcess(process);
		} catch (NoSessionException e) {
			// never happens because session is set before
		}
	}

	public static void deleteFile(NetworkManager networkManager, File file,
			UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config) {
		networkManager.setSession(new H2HSession(EncryptionUtil
				.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));

		try {
			DeleteFileProcess process = new DeleteFileProcess(file, networkManager);
			executeProcess(process);
		} catch (NoSessionException e) {
			// never happens because session is set before
		}
	}

	public static void moveFile(NetworkManager networkManager, File source, File destination,
			UserProfileManager profileManager, FileManager fileManager, IFileConfiguration config) {
		networkManager.setSession(new H2HSession(EncryptionUtil
				.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), profileManager, config, fileManager));

		try {
			MoveFileProcess process = new MoveFileProcess(networkManager, source, destination);
			executeProcess(process);
		} catch (NoSessionException e) {
			// never happens because session is set before
		}
	}
}
