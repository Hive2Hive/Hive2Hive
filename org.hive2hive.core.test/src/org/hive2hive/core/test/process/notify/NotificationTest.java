package org.hive2hive.core.test.process.notify;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessState;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.notify.NotifyPeersProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the notification procedure
 * 
 * @author Nico
 * 
 */
public class NotificationTest extends H2HJUnitTest {

	private static final int networkSize = 10;
	private static List<NetworkManager> network;
	private static final TestH2HFileConfiguration config = new TestH2HFileConfiguration();

	private static UserProfile userAProfile;
	private static UserCredentials userACredentials;

	private static UserCredentials userBCredentials;
	private static UserProfile userBProfile;

	private static UserCredentials userCCredentials;
	private static UserProfile userCProfile;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NotificationTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);

		// create 10 nodes:
		// node 0-2: user A
		// node 3-4: user B
		// node 5: user C
		userACredentials = NetworkTestUtil.generateRandomCredentials();
		userAProfile = ProcessTestUtil.register(network.get(0), userACredentials);

		userBCredentials = NetworkTestUtil.generateRandomCredentials();
		userBProfile = ProcessTestUtil.register(network.get(3), userBCredentials);

		userCCredentials = NetworkTestUtil.generateRandomCredentials();
		userCProfile = ProcessTestUtil.register(network.get(5), userCCredentials);

		// login all nodes
		login(network.get(0), userACredentials, userAProfile);
		login(network.get(1), userACredentials, userAProfile);
		login(network.get(2), userACredentials, userAProfile);
		login(network.get(3), userBCredentials, userBProfile);
		login(network.get(4), userBCredentials, userBProfile);
		login(network.get(5), userCCredentials, userCProfile);
	}

	/**
	 * Starts a login and creates a session at the network manager
	 * 
	 * @param networkManager
	 * @param credentials
	 * @param userProfile
	 */
	private static void login(NetworkManager networkManager, UserCredentials credentials,
			UserProfile userProfile) {
		// login with valid credentials
		LoginProcess process = new LoginProcess(credentials, networkManager);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		FileManager fileManager = new FileManager(FileUtils.getTempDirectory());
		H2HSession session = new H2HSession(userProfile.getEncryptionKeys(), new UserProfileManager(
				networkManager, credentials), config, fileManager);
		networkManager.setSession(session);
	}

	@Test
	public void testNotifyOwnUser() throws ClassNotFoundException, IOException {
		/**
		 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2)
		 */
		NetworkManager notifier = network.get(0);
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier);

		// send notification to own peers
		Set<String> users = new HashSet<String>(1);
		users.add(userAProfile.getUserId());
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, users, msgFactory);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!msgFactory.allMsgsArrived());

		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	@Test
	public void testNotifyOtherUsers() throws ClassNotFoundException, IOException {
		/**
		 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2) and also all clients of user B
		 * (peer 3 and 4)
		 */
		NetworkManager notifier = network.get(0);
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier);

		// send notification to own peers
		Set<String> users = new HashSet<String>(2);
		users.add(userAProfile.getUserId());
		users.add(userBProfile.getUserId());
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, users, msgFactory);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!msgFactory.allMsgsArrived());

		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	@Test
	public void testNotifyUnfriendlyLogout() throws ClassNotFoundException, IOException, InterruptedException {
		/**
		 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2) and also all clients of user B
		 * (peer 3 and 4). Peer 4 of user B has done an unfriendly leave, never responding.
		 */
		NetworkManager notifier = network.get(0);
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier);

		// send notification to own peers
		Set<String> users = new HashSet<String>(2);
		users.add(userAProfile.getUserId());
		users.add(userBProfile.getUserId());
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, users, msgFactory);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// kick out B
		network.get(4).disconnect();
		process.start();

		// wait until all messages are sent
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
		int sentMessages = msgFactory.getSentMessageCount();

		waiter = new H2HWaiter(5);
		do {
			waiter.tickASecond();
			// wait until all messages are here except 1
		} while (msgFactory.getArrivedMessageCount() != sentMessages - 1);
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
