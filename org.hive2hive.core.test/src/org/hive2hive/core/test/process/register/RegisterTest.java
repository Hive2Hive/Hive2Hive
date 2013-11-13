package org.hive2hive.core.test.process.register;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkPutGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
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
	public void testRegisterProcessSuccess() throws ClassNotFoundException, IOException, DataLengthException,
			IllegalStateException, InvalidCipherTextException {
		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		RegisterProcess process = new RegisterProcess(credentials, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// verify the new public key
		FutureGet getKey = otherClient.getGlobal(credentials.getUserId(), H2HConstants.USER_PUBLIC_KEY);
		getKey.awaitUninterruptibly();
		getKey.getFutureRequests().awaitUninterruptibly();
		UserPublicKey publicKey = (UserPublicKey) getKey.getData().object();
		Assert.assertNotNull(publicKey);
		// key should match
		Assert.assertEquals(process.getContext().getUserProfile().getEncryptionKeys().getPublic(),
				publicKey.getPublicKey());

		// verify the new user profile
		UserProfile gotUserProfile = NetworkPutGetUtil.getUserProfile(otherClient, credentials);
		Assert.assertNotNull(gotUserProfile);
		// profiles should match
		Assert.assertEquals(credentials.getUserId(), gotUserProfile.getUserId());

		// verify the new locations map
		FutureGet getLocations = otherClient.getGlobal(credentials.getUserId(), H2HConstants.USER_LOCATIONS);
		getLocations.awaitUninterruptibly();
		getLocations.getFutureRequests().awaitUninterruptibly();
		Locations locations = (Locations) getLocations.getData().object();
		Assert.assertNotNull(locations);
		// userId should match
		Assert.assertEquals(credentials.getUserId(), locations.getUserId());
		// fresh location maps should be empty
		Assert.assertTrue(locations.getLocationEntries().isEmpty());

		// verify the new user message queue
		FutureGet getQueue = otherClient.getGlobal(credentials.getUserId(),
				H2HConstants.USER_MESSAGE_QUEUE_KEY);
		getQueue.awaitUninterruptibly();
		getQueue.getFutureRequests().awaitUninterruptibly();
		UserMessageQueue queue = (UserMessageQueue) getQueue.getData().object();
		Assert.assertNotNull(queue);
		// userId should match
		Assert.assertEquals(credentials.getUserId(), queue.getUserId());
		// fresh queue should be empty
		// TODO uncomment
//		Assert.assertTrue(queue.getMessageQueue().isEmpty());
	}

	@Test
	public void testRegisterProcessProfileExists() {
		NetworkManager client = network.get(0);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		// already put a locations map
		FuturePut putProfile = client.putGlobal(credentials.getUserId(), H2HConstants.USER_LOCATIONS,
				new Locations(credentials.getUserId()));
		putProfile.awaitUninterruptibly();
		putProfile.getFutureRequests().awaitUninterruptibly();
		Assert.assertTrue(putProfile.isSuccess());

		RegisterProcess process = new RegisterProcess(credentials, client);
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
