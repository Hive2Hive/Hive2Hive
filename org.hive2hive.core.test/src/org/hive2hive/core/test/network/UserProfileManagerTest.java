package org.hive2hive.core.test.network;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.process.ProcessTestUtil;
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
	public void testGetOnly() {
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		TestGetUserProfileProcess proc1 = new TestGetUserProfileProcess();
		TestGetUserProfileProcess proc2 = new TestGetUserProfileProcess();
		TestGetUserProfileProcess proc3 = new TestGetUserProfileProcess();

		Assert.assertNotNull(manager.getUserProfile(proc1));
		Assert.assertNotNull(manager.getUserProfile(proc2));
		Assert.assertNotNull(manager.getUserProfile(proc3));
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

		public TestGetUserProfileProcess() {
			super(null);
			setNextStep(new GetUserProfileStep(userCredentials, null, null));
		}
	}
}
