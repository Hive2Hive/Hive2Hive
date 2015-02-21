package org.hive2hive.core.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserManagerTest extends H2HJUnitTest {

	private static List<IH2HNode> network;
	private static final Random rnd = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserManagerTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	@Before
	public void before() {
		beforeMethod();
		network = NetworkTestUtil.createH2HNetwork(3);
	}

	@After
	public void after() {
		NetworkTestUtil.shutdownH2HNetwork(network);
		afterMethod();
	}

	@Test
	public void isRegisteredTest() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException,
			ProcessExecutionException {
		UserCredentials userCredentials = generateRandomCredentials();
		String userId = userCredentials.getUserId();

		// all nodes must have same result: false
		for (IH2HNode client : network) {
			assertFalse(client.getUserManager().isRegistered(userId));
		}

		// registering from random node
		IUserManager userManager = network.get(rnd.nextInt(network.size())).getUserManager();
		IProcessComponent<Void> registerProcess = userManager.createRegisterProcess(userCredentials);
		registerProcess.execute();

		// all nodes must have same result: true
		for (IH2HNode client : network) {
			assertTrue(client.getUserManager().isRegistered(userId));
		}

		// TODO test after unregistering
	}

	@Test
	public void isLoggedInTest() throws NoPeerConnectionException, InterruptedException, NoSessionException,
			InvalidProcessStateException, ProcessExecutionException {
		UserCredentials userCredentials = generateRandomCredentials();

		TestFileAgent fileAgent = new TestFileAgent();

		// all nodes must have same result: false
		for (IH2HNode client : network) {
			assertFalse(client.getUserManager().isLoggedIn());
		}

		// before registering: login all nodes and check again
		IProcessComponent<Void> loginProcess;
		for (IH2HNode client : network) {
			loginProcess = client.getUserManager().createLoginProcess(userCredentials, fileAgent);
			try {
				loginProcess.execute();
				Assert.fail("Should fail to login when user is not registered");
			} catch (ProcessExecutionException e) {
				// expected
			}

			assertFalse(client.getUserManager().isLoggedIn());
		}

		// registering from random node
		IProcessComponent<Void> registerProcess = network.get(rnd.nextInt(network.size())).getUserManager()
				.createRegisterProcess(userCredentials);
		registerProcess.execute();

		// after registering: login all nodes and check again
		for (IH2HNode client : network) {
			loginProcess = client.getUserManager().createLoginProcess(userCredentials, fileAgent);
			loginProcess.execute();

			assertTrue(client.getUserManager().isLoggedIn());
		}

		// logout
		IProcessComponent<Void> logoutProcess;
		for (IH2HNode client : network) {
			logoutProcess = client.getUserManager().createLogoutProcess();
			logoutProcess.execute();

			assertFalse(client.getUserManager().isLoggedIn());
		}

		// TODO test after unregister
	}
}
