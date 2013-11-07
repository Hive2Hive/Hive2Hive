package org.hive2hive.core.test.process.login;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.security.UserPassword;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
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
	private static UserPassword userPassword;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LoginTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);

		String userId = NetworkTestUtil.randomString();
		String password = NetworkTestUtil.randomString();
		String pin = NetworkTestUtil.randomString();

		// register a user
		RegisterProcess process = new RegisterProcess(userId, password, pin, network.get(0));
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		userProfile = process.getUserProfile();
		userPassword = process.getUserPassword();
	}

	@Test
	public void testValidCredentials() throws ClassNotFoundException, IOException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// login with valid credentials
		LoginProcess process = new LoginProcess(userProfile.getUserId(), userPassword.getPassword(),
				userPassword.getPin(), client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		Assert.assertNotNull(process.getUserProfile());
		Assert.assertEquals(userProfile.getUserId(), process.getUserProfile().getUserId());

		// verify the locations map
		FutureGet futureGet = client.getGlobal(userProfile.getUserId(), H2HConstants.USER_LOCATIONS);
		futureGet.awaitUninterruptibly();
		futureGet.getFutureRequests().awaitUninterruptibly();

		Locations locations = (Locations) futureGet.getData().object();
		Assert.assertEquals(1, locations.getLocationsEntries().size());
	}

	@Test
	public void testInvalidPassword() {
		LoginProcess process = loginAndWaitToFail(userProfile.getUserId(), NetworkTestUtil.randomString(),
				userPassword.getPin());
		Assert.assertNull(process.getUserProfile());
	}

	@Test
	public void testInvalidPin() {
		LoginProcess process = loginAndWaitToFail(userProfile.getUserId(), userPassword.getPassword(),
				NetworkTestUtil.randomString());
		Assert.assertNull(process.getUserProfile());
	}

	@Test
	public void testInvalidUserId() {
		LoginProcess process = loginAndWaitToFail(NetworkTestUtil.randomString(), userPassword.getPassword(),
				userPassword.getPin());
		Assert.assertNull(process.getUserProfile());
	}

	public LoginProcess loginAndWaitToFail(String userId, String password, String pin) {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		LoginProcess process = new LoginProcess(userId, password, pin, client);
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
