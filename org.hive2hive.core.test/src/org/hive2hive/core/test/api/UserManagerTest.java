package org.hive2hive.core.test.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class UserManagerTest extends H2HJUnitTest {

	private static List<IH2HNode> network;
	
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserManagerTest.class;
		beforeClass();
		
		network = NetworkTestUtil.createH2HNetwork(5);
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownH2HNetwork(network);
		
		afterClass();
	}
	
	@Test
	public void isRegisteredTest() throws NoPeerConnectionException, InterruptedException {
		
		Random r = new Random();
		
		String userId = generateRandomString(20);
		UserCredentials userCredentials = new UserCredentials(userId, generateRandomString(8), generateRandomString(8));
		
		// all nodes must have same result: false
		for (int i = 0; i < network.size(); i++) {
			
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertFalse(isRegistered);
		}
		
		// registering from random node (await)
		IUserManager userManager = network.get(r.nextInt(network.size())).getUserManager();
		userManager.configureAutostart(true);
		userManager.register(userCredentials).await();
		
		// all nodes must have same result: true
		for (int i = 0; i < network.size(); i++) {
			
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertTrue(isRegistered);
		}
		
		// TODO test after unregistering
	}
	
	@Ignore
	@Test
	public void isLoggedInTest() throws NoPeerConnectionException, InterruptedException {
		
		// TODO check before registering
		
		String userId = generateRandomString(20);
		UserCredentials userCredentials = new UserCredentials(userId, generateRandomString(8), generateRandomString(8));
		Path rootPah = (new File("testRoot")).toPath();
		
		// all nodes must have same result: false
		for (int i = 0; i < network.size(); i++) {
			
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertFalse(isLoggedIn);
		}
		
		// login all nodes and check again
		for (int i = 0; i < network.size(); i++) {
			
			// before registering
			network.get(i).getUserManager().login(userCredentials, rootPah).await();
			boolean isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertFalse(isLoggedIn);
			
			// after registering
			network.get(i).getUserManager().register(userCredentials).await();
			network.get(i).getUserManager().login(userCredentials, rootPah).await();
			isLoggedIn = network.get(i).getUserManager().isLoggedIn(userId);
			assertTrue(isLoggedIn);
		}
		
		// TODO test after logout
	}
}
