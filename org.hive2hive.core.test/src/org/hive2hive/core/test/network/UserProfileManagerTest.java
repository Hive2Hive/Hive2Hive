package org.hive2hive.core.test.network;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserProfileManagerTest extends H2HJUnitTest {

	private UserCredentials userCredentials;
	private NetworkManager client;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileManagerTest.class;
		beforeClass();
	}

	@Before
	public void setup() {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = NetworkTestUtil.createNetwork(1).get(0);
		ProcessTestUtil.register(client, userCredentials);
	}

	@Test
	public void testGetOnly() throws GetFailedException, InterruptedException {
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		TestGetUserProfileProcess proc1 = new TestGetUserProfileProcess(manager, client);
		TestGetUserProfileProcess proc2 = new TestGetUserProfileProcess(manager, client);
		TestGetUserProfileProcess proc3 = new TestGetUserProfileProcess(manager, client);

		TestProcessListener listener1 = new TestProcessListener();
		proc1.addListener(listener1);
		TestProcessListener listener2 = new TestProcessListener();
		proc2.addListener(listener2);
		TestProcessListener listener3 = new TestProcessListener();
		proc3.addListener(listener3);

		// start, but not all at the same time
		proc1.start();
		Thread.sleep(200);
		proc2.start();
		Thread.sleep(250);
		proc3.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!(listener1.hasSucceeded() && listener2.hasSucceeded() && listener3.hasSucceeded()));
	}

	@After
	public void shutdown() {
		client.disconnect();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	private class TestGetUserProfileProcess extends Process {

		public TestGetUserProfileProcess(UserProfileManager profileManager, NetworkManager networkManager) {
			super(networkManager);
			setNextStep(new TestGetUserProfileStep(profileManager));
		}
	}

	private class TestGetUserProfileStep extends ProcessStep {

		private UserProfileManager profileManager;

		public TestGetUserProfileStep(UserProfileManager profileManager) {
			this.profileManager = profileManager;
		}

		@Override
		public void start() {
			try {
				profileManager.getUserProfile(getProcess());
				getProcess().setNextStep(null);
			} catch (GetFailedException e) {
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
