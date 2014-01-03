package org.hive2hive.core.test.process.login;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the login procedure. Should not be able to login if the credentials are wrong.
 * 
 * @author Nico
 * 
 */
public class LoginTest extends H2HJUnitTest {

	private static final int networkSize = 10;
	private static List<NetworkManager> network;
	private static UserProfile userProfile;
	private static UserCredentials userCredentials;
	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LoginTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		userProfile = ProcessTestUtil.register(userCredentials, network.get(0));
		root = FileUtils.getTempDirectory();
	}

	@Test
	public void testValidCredentials() throws ClassNotFoundException, IOException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// login with valid credentials
		UserProfile profile = ProcessTestUtil.login(userCredentials, client, root);

		Assert.assertNotNull(profile);
		Assert.assertEquals(userProfile.getUserId(), profile.getUserId());

		// verify the locations map
		FutureGet futureGet = client.getDataManager().get(Number160.createHash(userProfile.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();

		Locations locations = (Locations) futureGet.getData().object();
		Assert.assertEquals(1, locations.getPeerAddresses().size());
	}

	@Test
	public void testInvalidPassword() {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(),
				NetworkTestUtil.randomString(), userCredentials.getPin());

		LoginProcess process = loginAndWaitToFail(wrongCredentials);
		Assert.assertNull(process.getContext().getUserProfile());
	}

	@Test
	public void testInvalidPin() {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(),
				userCredentials.getPassword(), NetworkTestUtil.randomString());

		LoginProcess process = loginAndWaitToFail(wrongCredentials);
		Assert.assertNull(process.getContext().getUserProfile());
	}

	@Test
	public void testInvalidUserId() {
		UserCredentials wrongCredentials = new UserCredentials(NetworkTestUtil.randomString(),
				userCredentials.getPassword(), userCredentials.getPin());

		LoginProcess process = loginAndWaitToFail(wrongCredentials);
		Assert.assertNull(process.getContext().getUserProfile());
	}

	public LoginProcess loginAndWaitToFail(UserCredentials wrongCredentials) {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		SessionParameters sessionParameters = new SessionParameters();
		sessionParameters.setProfileManager(new UserProfileManager(client, wrongCredentials));

		LoginProcess process = new LoginProcess(wrongCredentials, sessionParameters, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		return process;
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
