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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserManagerTest extends H2HJUnitTest {

	private static List<IH2HNode> network;

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
		network = NetworkTestUtil.createH2HNetwork(5);
	}

	@After
	public void after() {
		NetworkTestUtil.shutdownH2HNetwork(network);
		afterMethod();
	}

	@Test
	public void isRegisteredTest() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, ProcessExecutionException {
		UserCredentials userCredentials = generateRandomCredentials();
		String userId = userCredentials.getUserId();

		// all nodes must have same result: false
		for (int i = 0; i < network.size(); i++) {
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertFalse(isRegistered);
		}

		// registering from random node
		IUserManager userManager = network.get(new Random().nextInt(network.size())).getUserManager();
		IProcessComponent<Void> registerProcess = userManager.createRegisterProcess(userCredentials);
		registerProcess.execute();
		
		// all nodes must have same result: true
		for (int i = 0; i < network.size(); i++) {
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertTrue(isRegistered);
		}

		// TODO test after unregistering
	}

	@Test
	public void isLoggedInTest() throws NoPeerConnectionException, InterruptedException, NoSessionException, InvalidProcessStateException, ProcessExecutionException {
		UserCredentials userCredentials = generateRandomCredentials();
		String userId = userCredentials.getUserId();

		TestFileAgent fileAgent = new TestFileAgent();

		// all nodes must have same result: false
		for (int i = 0; i < network.size(); i++) {
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertFalse(isLoggedIn);
		}

		// before registering: login all nodes and check again
		IProcessComponent<Void> loginProcess;
		for (int i = 0; i < network.size(); i++) {
			loginProcess = network.get(i).getUserManager().createLoginProcess(userCredentials, fileAgent);
			loginProcess.execute();
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertFalse(isLoggedIn);
		}

		// registering from random node
		IProcessComponent<Void> registerProcess = network.get(new Random().nextInt(network.size())).getUserManager().createRegisterProcess(userCredentials);
		registerProcess.execute();

		// after registering: login all nodes and check again
		for (int i = 0; i < network.size(); i++) {
			loginProcess = network.get(i).getUserManager().createLoginProcess(userCredentials, fileAgent);
			loginProcess.execute();
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertTrue(isLoggedIn);
		}

		// logout
		IProcessComponent<Void> logoutProcess;
		for (int i = 0; i < network.size(); i++) {
			logoutProcess = network.get(i).getUserManager().createLogoutProcess();
			logoutProcess.execute();
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertFalse(isLoggedIn);
		}

		// TODO test after unregister
	}
}
