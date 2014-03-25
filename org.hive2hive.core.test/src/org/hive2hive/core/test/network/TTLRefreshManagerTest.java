package org.hive2hive.core.test.network;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.TTLRefreshManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TTLRefreshManagerTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 3;
	private static UserCredentials userCredentials;
	private static File root;
	private static NetworkManager client;
	

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = TTLRefreshManagerTest.class;
		beforeClass();
		
		network = NetworkTestUtil.createNetwork(networkSize);
		
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		
		// register new random user
		UseCaseTestUtil.register(userCredentials, network.get(0));
		
		root = NetworkTestUtil.getTempDirectory();
		
		client = network.get(new Random().nextInt(networkSize));

		// login with valid credentials
		UseCaseTestUtil.login(userCredentials, client, root);
	}
	
	@Test
	public void test() throws InterruptedException, NoSessionException, NoPeerConnectionException{
		TTLRefreshManager ttlRefreshManager = new TTLRefreshManager(client.getSession().getProfileManager(), client.getDataManager());
		ttlRefreshManager.start();
		
		synchronized (this) {
			Thread.sleep(2000);			
		}
		
		ttlRefreshManager.stop();
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}