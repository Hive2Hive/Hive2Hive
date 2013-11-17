package org.hive2hive.core.test.network;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.download.GetFileChunkStep;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.process.TestProcessListener;

/**
 * Helper class for JUnit tests to get some documents from the DHT.
 * All methods are blocking until the result is here.
 * 
 * @author Nico
 * 
 */
public class NetworkPutGetUtil {

	private NetworkPutGetUtil() {
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

		H2HWaiter waiter = new H2HWaiter(20);
		do {
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

		H2HWaiter waiter = new H2HWaiter(2000);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	public static UserProfile register(NetworkManager networkManager, UserCredentials credentials) {
		RegisterProcess register = new RegisterProcess(credentials, networkManager);
		executeProcess(register);
		return register.getContext().getUserProfile();
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

		GetUserProfileStep step = new GetUserProfileStep(credentials, null, context);
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
			public void setLocation(Locations locations) {
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

	public static File downloadFile(NetworkManager networkManager, FileTreeNode file, MetaFile metaFile,
			FileManager fileManager) {
		GetFileChunkStep step = new GetFileChunkStep(file, metaFile, fileManager);
		executeStep(networkManager, step);
		return step.getFile();
	}

	public static File downloadFile(NetworkManager networkManager, FileTreeNode file, FileManager fileManager) {
		DownloadFileProcess process = new DownloadFileProcess(file, networkManager, fileManager);
		executeProcess(process);
		return fileManager.getFile(file);
	}

	public static void uploadNewFile(NetworkManager networkManager, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		NewFileProcess process = new NewFileProcess(file, credentials, networkManager, fileManager, config);
		executeProcess(process);
	}

	public static void uploadNewFileVersion(NetworkManager networkManager, File file,
			UserCredentials credentials, FileManager fileManager, IH2HFileConfiguration config) {
		NewVersionProcess process = new NewVersionProcess(file, credentials, networkManager, fileManager,
				config);
		executeProcess(process);
	}
}
