package org.hive2hive.core.test.process.common.get;

import static org.junit.Assert.assertFalse;

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
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkPutGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the user profile into the DHT
 * 
 * @author Nico, Seppi
 * 
 */
public class GetUserProfileStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetUserProfileStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccess() throws InterruptedException, InvalidKeySpecException, DataLengthException,
			IllegalStateException, InvalidCipherTextException, ClassNotFoundException, IOException {
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
		FuturePut putGlobal = putter.getDataManager().putGlobal(credentials.getProfileLocationKey(),
				H2HConstants.USER_PROFILE, encrypted);
		putGlobal.awaitUninterruptibly();

		UserProfile profile = NetworkPutGetUtil.getUserProfile(putter, credentials);

		// verify if both objects are the same
		Assert.assertEquals(credentials.getUserId(), profile.getUserId());
	}

	@Test
	public void testStepSuccessWithNoUserProfile() {
		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		TestGetUserProfileContext context = new TestGetUserProfileContext();
		
		GetUserProfileStep getStep = new GetUserProfileStep(credentials, null, context);

		Process process = new Process(network.get(0)) {
		};
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.setNextStep(getStep);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
		
		Assert.assertNull(context.getUserProfile());
	}

	private class TestGetUserProfileContext implements IGetUserProfileContext {

		private UserProfile userProfile;

		@Override
		public void setUserProfile(UserProfile userProfile) {
			this.userProfile = userProfile;
		}

		@Override
		public UserProfile getUserProfile() {
			return userProfile;
		}
	};

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
