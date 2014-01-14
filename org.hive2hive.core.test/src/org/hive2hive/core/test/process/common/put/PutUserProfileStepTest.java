package org.hive2hive.core.test.process.common.put;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.register.PutUserProfileStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the user profile into the DHT.
 * 
 * @author Nico, Seppi
 * 
 */
public class PutUserProfileStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutUserProfileStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccessful() throws InterruptedException, InvalidKeySpecException,
			DataLengthException, IllegalStateException, InvalidCipherTextException, ClassNotFoundException,
			IOException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager client = network.get(1); // where the user profile is stored

		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		UserProfile testProfile = new UserProfile(credentials.getUserId());

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutUserProfileStep step = new PutUserProfileStep(testProfile, credentials, null);
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// get the user profile which should be stored at the proxy
		FutureGet global = client.getDataManager().get(
				Number160.createHash(credentials.getProfileLocationKey()), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(H2HConstants.USER_PROFILE));
		global.awaitUninterruptibly();
		global.getFutureRequests().awaitUninterruptibly();
		EncryptedNetworkContent found = (EncryptedNetworkContent) global.getData().object();
		Assert.assertNotNull(found);

		// decrypt it using the same password as set above
		SecretKey decryptionKeys = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
				credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);
		UserProfile decrypted = (UserProfile) H2HEncryptionUtil.decryptAES(found, decryptionKeys);

		// verify if both objects are the same
		Assert.assertEquals(credentials.getUserId(), decrypted.getUserId());
	}

	@Test
	public void testStepRollback() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		UserProfile testProfile = new UserProfile(credentials.getUserId());

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutUserProfileStep step = new PutUserProfileStep(testProfile, credentials, null);
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());

		// get the locations which should be stored at the proxy --> they should be null
		FutureGet futureGet = proxy.getDataManager().get(
				Number160.createHash(credentials.getProfileLocationKey()), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
