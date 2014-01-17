package org.hive2hive.core.test.process.notify;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.ProcessState;
import org.hive2hive.core.process.notify.NotifyPeersProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
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
	private static final TestFileConfiguration config = new TestFileConfiguration();

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

		// create 10 nodes and login 5 of them:
		// node 0-2: user A
		// node 3-4: user B
		// node 5: user C
		userACredentials = NetworkTestUtil.generateRandomCredentials();
		userAProfile = ProcessTestUtil.register(userACredentials, network.get(0));

		userBCredentials = NetworkTestUtil.generateRandomCredentials();
		userBProfile = ProcessTestUtil.register(userBCredentials, network.get(3));

		userCCredentials = NetworkTestUtil.generateRandomCredentials();
		userCProfile = ProcessTestUtil.register(userCCredentials, network.get(5));

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
		File root = FileUtils.getTempDirectory();

		// login with valid credentials
		ProcessTestUtil.login(credentials, networkManager, root);

		FileManager fileManager = new FileManager(root.toPath());
		H2HSession session = new H2HSession(userProfile.getEncryptionKeys(), new UserProfileManager(
				networkManager, credentials), config, fileManager);
		networkManager.setSession(session);
	}

	/**
	 * Scenario: Call the notification process with an empty list
	 */
	@Test
	public void testNotifyNobody() throws ClassNotFoundException, IOException {
		NetworkManager notifier = network.get(0);
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				new HashSet<String>(0));
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait until all messages are sent
		ProcessTestUtil.waitTillSucceded(listener, 10);
	}

	/**
	 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2)
	 */
	@Test
	public void testNotifyOwnUser() throws ClassNotFoundException, IOException {
		NetworkManager notifier = network.get(0);

		// send notification to own peers
		Set<String> users = new HashSet<String>(1);
		users.add(userAProfile.getUserId());
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				users);
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!msgFactory.allMsgsArrived());

		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	/**
	 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2). Use the session of the current
	 * user here for performance improvements
	 */
	@Test
	public void testNotifyOwnUserSession() throws ClassNotFoundException, IOException, NoSessionException {
		NetworkManager notifier = network.get(0);
		// send notification to own peers
		Set<String> users = new HashSet<String>(1);
		users.add(userAProfile.getUserId());
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				users);
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!msgFactory.allMsgsArrived());

		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	/**
	 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2) and also all clients of user B
	 * (peer 3 and 4) and user C (peer 5)
	 */
	@Test
	public void testNotifyOtherUsers() throws ClassNotFoundException, IOException {
		NetworkManager notifier = network.get(0);
		// send notification to own peers
		Set<String> users = new HashSet<String>(2);
		users.add(userAProfile.getUserId());
		users.add(userBProfile.getUserId());
		users.add(userCProfile.getUserId());
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				users);
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!msgFactory.allMsgsArrived());

		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	/**
	 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2) and also all clients of user B
	 * (peer 3 and 4). Peer 4 of user B has done an unfriendly leave, never responding.
	 */
	@Test
	public void testNotifyUnfriendlyLogout() throws ClassNotFoundException, IOException, InterruptedException {
		NetworkManager notifier = network.get(0);

		// send notification to own peers
		Set<String> users = new HashSet<String>(2);
		users.add(userAProfile.getUserId());
		users.add(userBProfile.getUserId());
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				users);
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// kick out B
		network.get(4).disconnect();
		process.start();

		// wait until all messages are sent
		ProcessTestUtil.waitTillSucceded(listener, 20);
		int sentMessages = msgFactory.getSentMessageCount();

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
			// wait until all messages are here except 1
		} while (msgFactory.getArrivedMessageCount() != sentMessages - 1);
	}

	/**
	 * Scenario: User A (peer 0) contacts his own clients (peer 1 and 2), but peer 1 has done an unfriendly
	 * leave. The locations map should be cleaned up
	 */
	@Test
	public void testNotifyUnfriendlyLogoutOwnPeer() throws ClassNotFoundException, IOException,
			InterruptedException {
		NetworkManager notifier = network.get(0);

		// send notification to own peers
		Set<String> users = new HashSet<String>(2);
		users.add(userAProfile.getUserId());
		CountingNotificationMessageFactory msgFactory = new CountingNotificationMessageFactory(notifier,
				users);
		NotifyPeersProcess process = new NotifyPeersProcess(notifier, msgFactory);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// kick out Peer 1
		network.get(1).disconnect();
		process.start();

		// wait until all messages are sent
		ProcessTestUtil.waitTillSucceded(listener, 20);

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
			// wait until all processes (inclusive cleanup) are done
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// check the locations map; should have 2 entries only
		Locations locations = ProcessTestUtil.getLocations(network.get(0), userAProfile.getUserId());
		Assert.assertEquals(2, locations.getPeerAddresses().size());
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
