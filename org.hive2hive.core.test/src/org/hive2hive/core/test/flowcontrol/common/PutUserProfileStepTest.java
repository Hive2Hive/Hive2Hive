package org.hive2hive.core.test.flowcontrol.common;

import java.util.List;

import javax.crypto.SecretKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedContent;
import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.encryption.ProfileEncryptionUtil;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.PutUserProfileStep;
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

/**
 * Tests the generic step that puts the user profile into the DHT
 * 
 * @author Nico
 * 
 */
public class PutUserProfileStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutUserProfileStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccessful() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects
		String userId = proxy.getNodeId();
		String password = NetworkTestUtil.randomString();
		UserPassword userPassword = ProfileEncryptionUtil.createUserPassword(password);
		UserProfile testProfile = new UserProfile(userId,
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024),
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024));

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutUserProfileStep step = new PutUserProfileStep(testProfile, null, userPassword, null);
		process.setFirstStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// get the user profile which should be stored at the proxy
		EncryptedContent found = (EncryptedContent) proxy.getLocal(userId, H2HConstants.USER_PROFILE);
		Assert.assertNotNull(found);

		// decrypt it using the same password as set above
		SecretKey decryptionKeys = EncryptionUtil.createAESKeyFromPassword(userPassword,
				AES_KEYLENGTH.BIT_128);
		UserProfile decrypted = EncryptionUtil.decryptAES(found, decryptionKeys, UserProfile.class);

		// verify if both objects are the same
		Assert.assertEquals(userId, decrypted.getUserId());
		Assert.assertEquals(testProfile.getTimestamp(), decrypted.getTimestamp());
	}

	@Test
	public void testStepRollback() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects
		String userId = proxy.getNodeId();
		String password = NetworkTestUtil.randomString();
		UserPassword userPassword = ProfileEncryptionUtil.createUserPassword(password);
		UserProfile nnewProfile = new UserProfile(userId,
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024),
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024));

		UserProfile originalProfile = new UserProfile(userId,
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024),
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_1024));

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutUserProfileStep step = new PutUserProfileStep(nnewProfile, originalProfile, userPassword, null);
		process.setFirstStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// rollback
		process.rollBack("Testing the rollback");

		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		UserProfile gotProfile = (UserProfile) proxy.getLocal(userId, H2HConstants.USER_PROFILE);
		// check that restored profile is the same as the 'original'
		Assert.assertEquals(userId, gotProfile.getUserId());
		Assert.assertEquals(originalProfile.getEncryptionKeys().getPrivate(), gotProfile.getEncryptionKeys()
				.getPrivate());
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
