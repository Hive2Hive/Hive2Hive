package org.hive2hive.core.test.flowcontrol.register;

import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedNetworkContent;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.flowcontrol.TestProcessListener;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegisterTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RegisterTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRegisterProcessSuccess() {
		NetworkManager client = network.get(0);
		NetworkManager proxy = network.get(1);

		String userId = proxy.getNodeId();
		String password = NetworkTestUtil.randomString();

		RegisterProcess process = new RegisterProcess(userId, password, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// verify the new public key, the user profile and the locations map
		UserPublicKey publicKey = (UserPublicKey) proxy.getLocal(userId, H2HConstants.USER_PUBLIC_KEY);
		EncryptedNetworkContent content = (EncryptedNetworkContent) proxy.getLocal(userId, H2HConstants.USER_PROFILE);
		Locations locations = (Locations) proxy.getLocal(userId, H2HConstants.USER_LOCATIONS);

		Assert.assertNotNull(publicKey);
		// TODO: How to decrypt the profile correctly (with password only, without salt)
		Assert.assertNotNull(content);
		Assert.assertNotNull(locations);

		// key should match
		Assert.assertEquals(process.getUserProfile().getEncryptionKeys().getPublic(),
				publicKey.getPublicKey());

		// userId should match
		// Assert.assertEquals(userId, profile.getUserId());
		Assert.assertEquals(userId, locations.getUserId());

		// fresh location maps should be empty
		Assert.assertTrue(locations.getOnlinePeers().isEmpty());
	}

	@Test
	public void testRegisterProcessProfileExists() {
		NetworkManager client = network.get(0);
		NetworkManager proxy = network.get(1);

		String userId = proxy.getNodeId();
		String password = NetworkTestUtil.randomString();

		// already put a profile
		client.putGlobal(userId, H2HConstants.USER_PROFILE, new UserProfile(userId, null, null));

		RegisterProcess process = new RegisterProcess(userId, password, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
}
