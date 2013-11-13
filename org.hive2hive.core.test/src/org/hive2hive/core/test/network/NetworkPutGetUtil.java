package org.hive2hive.core.test.network;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.process.download.GetFileChunkStep;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.security.UserCredentials;
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

		H2HWaiter waiter = new H2HWaiter(20);
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
		GetUserProfileStep step = new GetUserProfileStep(credentials, null);
		executeStep(networkManager, step);
		return step.getUserProfile();
	}

	public static void putUserProfile(NetworkManager networkManager, UserProfile profile,
			UserCredentials credentials) {
		PutUserProfileStep step = new PutUserProfileStep(profile, credentials, null);
		executeStep(networkManager, step);
	}

	public static MetaDocument getMetaDocument(NetworkManager networkManager, KeyPair keys) {
		GetMetaDocumentStep step = new GetMetaDocumentStep(keys, null);
		executeStep(networkManager, step);
		return step.getMetaDocument();
	}

	public static File downloadFile(NetworkManager networkManager, FileTreeNode file, MetaFile metaFile,
			FileManager fileManager) {
		GetFileChunkStep step = new GetFileChunkStep(file, metaFile, fileManager);
		executeStep(networkManager, step);
		return step.getFile();
	}

	public static void uploadNewFile(NetworkManager networkManager, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		NewFileProcess process = new NewFileProcess(file, credentials, networkManager, fileManager, config);
		executeProcess(process);
	}
}
