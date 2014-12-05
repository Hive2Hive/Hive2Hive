package org.hive2hive.core.network.data;

import static org.junit.Assert.assertEquals;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test to check the {@link PublicKeyManager}.
 * 
 * @author Seppi
 * @author Nico
 */
public class PublicKeyManagerTest extends H2HJUnitTest {

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	private PublicKeyManager publicKeyManager;
	private KeyPair loggedInUserKeys;
	private String loggedInUserId;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PublicKeyManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void createKeyManager() throws NoPeerConnectionException {
		loggedInUserId = randomString();
		loggedInUserKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
		KeyPair loggedInProtectionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION);

		NetworkManager node = NetworkTestUtil.getRandomNode(network);
		publicKeyManager = new PublicKeyManager(loggedInUserId, loggedInUserKeys, loggedInProtectionKeys,
				node.getDataManager());
	}

	@Test
	public void testGettingLocalUserKeys() throws GetFailedException, NoPeerConnectionException {
		// check if the public key manager returns correctly the key of the logged in user
		assertEquals(loggedInUserKeys.getPublic(), publicKeyManager.getPublicKey(loggedInUserId));
	}

	@Test
	public void testFetchingFromNetwork() throws GetFailedException, NoPeerConnectionException {
		// create and upload some fake public keys into the network
		Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();
		for (int i = 0; i < random.nextInt(10); i++) {
			String userId = randomString();
			KeyPair key = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
			UserPublicKey userPublicKey = new UserPublicKey(key.getPublic());
			Parameters parameters = new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_PUBLIC_KEY)
					.setNetworkContent(userPublicKey);
			Assert.assertEquals(H2HPutStatus.OK, NetworkTestUtil.getRandomNode(network).getDataManager().put(parameters));
			publicKeys.put(userId, key.getPublic());
		}

		// check if the public key manager correctly fetches all public keys
		for (String userId : publicKeys.keySet()) {
			assertEquals(publicKeys.get(userId), publicKeyManager.getPublicKey(userId));
		}
	}

	@Test
	public void testCachingOfPublicKeys() throws GetFailedException, NoPeerConnectionException {
		Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();
		for (int i = 0; i < random.nextInt(5); i++) {
			String userId = randomString();
			KeyPair key = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
			UserPublicKey userPublicKey = new UserPublicKey(key.getPublic());
			Parameters parameters = new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_PUBLIC_KEY)
					.setNetworkContent(userPublicKey);
			Assert.assertEquals(H2HPutStatus.OK, NetworkTestUtil.getRandomNode(network).getDataManager().put(parameters));
			publicKeys.put(userId, key.getPublic());
		}

		for (String userId : publicKeys.keySet()) {
			assertEquals(publicKeys.get(userId), publicKeyManager.getPublicKey(userId));

			// remove the public keys from network, the manager shouldn't do any get request
			Parameters parameters = new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_PUBLIC_KEY);
			NetworkTestUtil.getRandomNode(network).getDataManager().removeUnblocked(parameters).awaitUninterruptibly();

			// the public key manager should use his cache
			assertEquals(publicKeys.get(userId), publicKeyManager.getPublicKey(userId));
		}
	}

	@Test(expected = GetFailedException.class)
	public void testNonExistingPublicKey() throws GetFailedException {
		String nonExistingUserId = randomString();
		publicKeyManager.getPublicKey(nonExistingUserId);
	}

	@Test(expected = GetFailedException.class)
	public void testGetFailedExceptions() throws NoPeerConnectionException, GetFailedException {
		String otherUser = randomString();
		H2HTestData noPublicKey = new H2HTestData("public key");
		Parameters parameters = new Parameters().setLocationKey(otherUser).setContentKey(H2HConstants.USER_PUBLIC_KEY)
				.setNetworkContent(noPublicKey);
		Assert.assertEquals(H2HPutStatus.OK, NetworkTestUtil.getRandomNode(network).getDataManager().put(parameters));

		publicKeyManager.getPublicKey(otherUser);
	}

	@Test
	public void testAllMixed() throws GetFailedException, NoPeerConnectionException {
		// create and upload some fake public keys into the network
		Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();
		for (int i = 0; i < 5; i++) {
			String userId = randomString();
			KeyPair key = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
			UserPublicKey userPublicKey = new UserPublicKey(key.getPublic());
			Parameters parameters = new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_PUBLIC_KEY)
					.setNetworkContent(userPublicKey);
			Assert.assertEquals(H2HPutStatus.OK, NetworkTestUtil.getRandomNode(network).getDataManager().put(parameters));
			publicKeys.put(userId, key.getPublic());
		}

		// check if the public key manager correctly fetches all public keys
		List<String> userIds = new ArrayList<String>(publicKeys.keySet());
		List<String> gettingList = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			gettingList.add(userIds.get(random.nextInt(userIds.size())));
		}

		// get several times the public key
		for (String userId : gettingList) {
			assertEquals(publicKeys.get(userId), publicKeyManager.getPublicKey(userId));
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
