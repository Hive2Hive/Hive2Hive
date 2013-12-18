package org.hive2hive.core.test.network.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link UserProfileManager} which handles concurrency when editing a user profile with multiple
 * processes. Many combinations of get, put and modify are tested.
 * 
 * @author Nico
 * 
 */
public class UserProfileManagerTest extends H2HJUnitTest {

	private UserCredentials userCredentials;
	private NetworkManager client;

	private enum Operation {
		PUT,
		GET,
		MODIFY
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileManagerTest.class;
		beforeClass();
	}

	@Before
	public void setup() {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = NetworkTestUtil.createNetwork(1).get(0);
		ProcessTestUtil.register(userCredentials, client);
	}

	@Test
	public void testGetOnly() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.GET, Operation.GET, Operation.GET, Operation.GET);
	}

	@Test
	public void testPutSingle() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.GET, Operation.PUT, Operation.GET);
	}

	@Test
	public void testPutMultiple() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.PUT, Operation.PUT, Operation.PUT);
	}

	@Test
	public void testModifySingle() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.PUT, Operation.MODIFY, Operation.PUT);
	}

	@Test
	public void testModifyMultiple() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.MODIFY, Operation.MODIFY, Operation.MODIFY);
	}

	@Test
	public void testAllMixed() throws GetFailedException, InterruptedException {
		executeProcesses(Operation.PUT, Operation.GET, Operation.MODIFY, Operation.GET, Operation.PUT,
				Operation.MODIFY, Operation.MODIFY, Operation.GET, Operation.GET, Operation.PUT,
				Operation.PUT, Operation.GET);
	}

	/**
	 * Transforms the operations into a set of processes and starts them all. The processes are started with a
	 * small delay, but in the same order as the parameters. The method blocks until all processes are done.
	 */
	private void executeProcesses(Operation... operations) throws GetFailedException, InterruptedException {
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		List<TestUserProfileProcess> processes = new ArrayList<TestUserProfileProcess>(operations.length);
		List<TestProcessListener> listeners = new ArrayList<TestProcessListener>(operations.length);

		for (int i = 0; i < operations.length; i++) {
			TestUserProfileProcess proc = new TestUserProfileProcess(manager, client, operations[i]);
			TestProcessListener listener = new TestProcessListener();
			proc.addListener(listener);

			processes.add(proc);
			listeners.add(listener);
		}

		// start, but not all at the same time
		for (TestUserProfileProcess process : processes) {
			process.start();
			// sleep for random time
			Thread.sleep(Math.abs(new Random().nextLong() % 100));
		}

		H2HWaiter waiter = new H2HWaiter(20);
		boolean allFinished;
		do {
			waiter.tickASecond();
			allFinished = true;

			for (TestProcessListener listener : listeners) {
				allFinished &= listener.hasSucceeded();
			}
		} while (!allFinished);
	}

	@After
	public void shutdown() {
		client.disconnect();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	/**
	 * Test class that executes a single step getting the user profile
	 * 
	 * @author Nico
	 * 
	 */
	private class TestUserProfileProcess extends Process {

		public TestUserProfileProcess(UserProfileManager profileManager, NetworkManager networkManager,
				Operation operation) {
			super(networkManager);
			switch (operation) {
				case PUT:
					setNextStep(new TestUserProfileStep(profileManager, true, false));
					break;
				case GET:
					setNextStep(new TestUserProfileStep(profileManager, false, false));
					break;
				case MODIFY:
					setNextStep(new TestUserProfileStep(profileManager, true, true));
					break;
				default:
					Assert.fail();
					break;
			}
		}
	}

	/**
	 * Gets the user profile using the {@link UserProfileManager}.
	 * 
	 * @author Nico
	 * 
	 */
	private class TestUserProfileStep extends ProcessStep {

		private final UserProfileManager profileManager;
		private final boolean modify;
		private final boolean put;

		/**
		 * 
		 * @param profileManager
		 * @param put if true, it performs a put operation
		 * @param modify if true, it does a modification
		 */
		public TestUserProfileStep(UserProfileManager profileManager, boolean put, boolean modify) {
			this.profileManager = profileManager;
			this.put = put;
			this.modify = modify;
		}

		@Override
		public void start() {
			try {
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), put);

				if (modify) {
					new FileTreeNode(userProfile.getRoot(), null, NetworkTestUtil.randomString());
				}

				if (put) {
					profileManager.readyToPut(userProfile, getProcess().getID());
				}

				getProcess().setNextStep(null);
			} catch (GetFailedException | PutFailedException e) {
				getProcess().stop(e.getMessage());
				Assert.fail();
			}
		}

		@Override
		public void rollBack() {
			Assert.fail();
		}
	}
}
