package org.hive2hive.core.test.process.common.get;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FuturePut;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the user profile into the DHT
 * 
 * @author Nico
 * 
 */
public class GetUserProfileStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetUserProfileStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccessful() throws InterruptedException, InvalidKeySpecException,
			DataLengthException, IllegalStateException, InvalidCipherTextException, ClassNotFoundException,
			IOException {
		NetworkManager putter = network.get(0); // where the process runs

		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		UserProfile testProfile = new UserProfile(credentials.getUserId(),
				EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_1024),
				EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_1024));

		// add them already to the DHT
		SecretKey encryptionKeys = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
				credentials.getPin(), AES_KEYLENGTH.BIT_256);
		EncryptedNetworkContent encrypted = H2HEncryptionUtil.encryptAES(testProfile, encryptionKeys);
		FuturePut putGlobal = putter.putGlobal(UserProfile.getLocationKey(credentials),
				H2HConstants.USER_PROFILE, encrypted);
		putGlobal.awaitUninterruptibly();
		putGlobal.getFutureRequests().awaitUninterruptibly();

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		GetUserProfileStep step = new GetUserProfileStep(credentials, null);
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		UserProfile profile = step.getUserProfile();

		// verify if both objects are the same
		Assert.assertEquals(credentials.getUserId(), profile.getUserId());
		Assert.assertEquals(testProfile.getTimestamp(), profile.getTimestamp());
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
