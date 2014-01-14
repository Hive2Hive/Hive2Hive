package org.hive2hive.core.test.network.data;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link UserProfileManager}'s exception triggering ({@link GetFailedException} and
 * {@link PutFailedException}).
 * 
 * @author Seppi
 */
public class UserProfileManagerExceptionHandlingTest extends H2HJUnitTest {

	private UserCredentials userCredentials;
	private NetworkManager client;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileManagerExceptionHandlingTest.class;
		beforeClass();
	}

	@Before
	public void setup() {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = NetworkTestUtil.createNetwork(1).get(0);
	}

	@Test
	public void testGetException() {
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		try {
			profileManager.getUserProfile(-1, false);
			Assert.fail();
		} catch (GetFailedException e) {
			// has to be thrown
		}
	}

	@Test
	public void testPutException() throws GetFailedException {
		ProcessTestUtil.register(userCredentials, client);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		UserProfile userProfile = profileManager.getUserProfile(-1, true);
		
		// modify the version key to trigger a version conflict (wrong based on key)
		userProfile.generateVersionKey();

		new FileTreeNode(userProfile.getRoot(), null, NetworkTestUtil.randomString());

		try {
			profileManager.readyToPut(userProfile, -1);
			Assert.fail();
		} catch (PutFailedException e) {
			// has to be thrown
		}
	}

	@After
	public void shutdown() {
		client.disconnect();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
