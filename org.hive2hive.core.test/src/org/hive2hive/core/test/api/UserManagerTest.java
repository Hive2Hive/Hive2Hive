package org.hive2hive.core.test.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
		
		// all nodes in network must have same result: false
		for (int i = 0; i < network.size(); i++) {
			
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertFalse(isRegistered);
		}
		
		// registering from random node (await)
		network.get(r.nextInt(network.size())).getUserManager().register(userCredentials).await();
		
		
		// all nodes in network must have same result: true
		for (int i = 0; i < network.size(); i++) {
			
			boolean isRegistered = network.get(i).getUserManager().isRegistered(userId);
			assertTrue(isRegistered);
		}
		
		// TODO test after unregistering
		
		
	}
}
