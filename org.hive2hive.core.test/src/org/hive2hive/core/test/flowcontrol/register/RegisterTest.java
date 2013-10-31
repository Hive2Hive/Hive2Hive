package org.hive2hive.core.test.flowcontrol.register;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedNetworkContent;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.H2HEncryptionUtil;
import org.hive2hive.core.encryption.PasswordUtil;
import org.hive2hive.core.encryption.UserPassword;
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
	public void testRegisterProcessSuccess() throws ClassNotFoundException, IOException, DataLengthException,
			IllegalStateException, InvalidCipherTextException {
		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);

		String userId = NetworkTestUtil.randomString();
		String password = NetworkTestUtil.randomString();
		String pin = generateRandomString(6);

		RegisterProcess process = new RegisterProcess(userId, password, pin, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// get the user profile and the password from the process
		UserProfile userProfile = process.getUserProfile();
		UserPassword userPassword = process.getUserPassword();
		String profileLocation = userProfile.getLocationKey(userPassword);

		// verify the new public key
		FutureGet getKey = otherClient.getGlobal(userId, H2HConstants.USER_PUBLIC_KEY);
		getKey.awaitUninterruptibly();
		getKey.getFutureRequests().awaitUninterruptibly();
		UserPublicKey publicKey = (UserPublicKey) getKey.getData().object();
		Assert.assertNotNull(publicKey);
		// key should match
		Assert.assertEquals(process.getUserProfile().getEncryptionKeys().getPublic(),
				publicKey.getPublicKey());

		// verify the new user profile
		FutureGet getProfile = otherClient.getGlobal(profileLocation, H2HConstants.USER_PROFILE);
		getProfile.awaitUninterruptibly();
		getProfile.getFutureRequests().awaitUninterruptibly();
		EncryptedNetworkContent content = (EncryptedNetworkContent) getProfile.getData().object();
		Assert.assertNotNull(content);
		// decrypt it
		SecretKey aesKeyFromPassword = PasswordUtil.generateAESKeyFromPassword(userPassword,
				AES_KEYLENGTH.BIT_256);
		UserProfile gotUserProfile = (UserProfile) H2HEncryptionUtil.decryptAES(content, aesKeyFromPassword);
		Assert.assertNotNull(gotUserProfile);
		// profiles should match
		Assert.assertEquals(userId, gotUserProfile.getUserId());

		// verify the new locations map
		FutureGet getLocations = otherClient.getGlobal(userId, H2HConstants.USER_LOCATIONS);
		getLocations.awaitUninterruptibly();
		getLocations.getFutureRequests().awaitUninterruptibly();
		Locations locations = (Locations) getLocations.getData().object();
		Assert.assertNotNull(locations);
		// userId should match
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
		String pin = generateRandomString(6);

		// already put a profile
		UserProfile profile = new UserProfile(userId, null, null);
		UserPassword userPassword = new UserPassword(password, pin);
		FuturePut putProfile = client.putGlobal(profile.getLocationKey(userPassword),
				H2HConstants.USER_PROFILE, new UserProfile(userId, null, null));
		putProfile.awaitUninterruptibly();
		putProfile.getFutureRequests().awaitUninterruptibly();

		RegisterProcess process = new RegisterProcess(userId, password, pin, client);
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
