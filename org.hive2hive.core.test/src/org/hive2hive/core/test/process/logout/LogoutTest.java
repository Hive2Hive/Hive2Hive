package org.hive2hive.core.test.process.logout;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.logout.LogoutProcess;
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
 * Tests the logout procedure.
 * 
 * @author Christian
 * 
 */
public class LogoutTest extends H2HJUnitTest {

	private static final int networkSize = 10;
	private static List<NetworkManager> network;
	private static UserProfile userProfile;
	private static UserCredentials userCredentials;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LogoutTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		userProfile = ProcessTestUtil.register(userCredentials, network.get(0));
	}

	@Test
	public void testLogout() throws ClassNotFoundException, IOException {

		NetworkManager client = network.get(new Random().nextInt(networkSize));
		ProcessTestUtil.login(userCredentials, client);

		// verify the locations map before logout
		FutureGet futureGet = client.getDataManager().get(Number160.createHash(userProfile.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();
		futureGet.getFutureRequests().awaitUninterruptibly();
		Locations locations = (Locations) futureGet.getData().object();
		
		System.out.println("\nLOCATIONS: " + locations.getPeerAddresses());
		System.out.println("Client PeerAddress: " + client.getPeerAddress() +"\n");
		
		Assert.assertTrue(locations.getPeerAddresses().contains(client.getPeerAddress()));
		

		// logout
		LogoutProcess process = new LogoutProcess(userCredentials.getUserId(), client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		assertNotNull(process.getContext().getLocations());

		// verify the locations map after logout
		FutureGet futureGet2 = client.getDataManager().get(Number160.createHash(userProfile.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet2.awaitUninterruptibly();
		futureGet2.getFutureRequests().awaitUninterruptibly();
		Locations locations2 = (Locations) futureGet2.getData().object();

		System.out.println("\nLOCATIONS: " + locations2.getPeerAddresses() +"\n");
		
		Assert.assertFalse(locations2.getPeerAddresses().contains(client.getPeerAddress()));
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
