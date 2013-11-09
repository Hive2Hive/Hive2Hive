package org.hive2hive.core.test.network;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
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
public class NetworkGetUtil {

	private NetworkGetUtil() {
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

	public static UserProfile getUserProfile(NetworkManager networkManager, UserCredentials credentials) {
		GetUserProfileStep step = new GetUserProfileStep(credentials, null);
		executeStep(networkManager, step);
		return step.getUserProfile();
	}

	public static MetaDocument getMetaDocument(NetworkManager networkManager, KeyPair keys) {
		GetMetaDocumentStep step = new GetMetaDocumentStep(keys, null);
		executeStep(networkManager, step);
		return step.getMetaDocument();
	}
}
