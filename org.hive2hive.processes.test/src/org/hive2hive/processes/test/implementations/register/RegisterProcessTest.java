package org.hive2hive.processes.test.implementations.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.processes.framework.concretes.ProcessListener;
import org.hive2hive.processes.framework.decorators.AsyncComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.register.RegisterProcess;
import org.hive2hive.processes.implementations.register.RegisterProcessContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RegisterProcessTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int NETWORK_SIZE = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RegisterProcessTest.class;
		beforeClass();
	}

	@Override
	public void beforeMethod() {
		super.beforeMethod();

		network = NetworkTestUtil.createNetwork(NETWORK_SIZE);
	}
	
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	@Ignore
	@Test
	public void testRegisterProcessSuccess() throws InvalidProcessStateException, ClassNotFoundException,
			IOException {

		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		UserProfile profile = new UserProfile(credentials.getUserId());

		RegisterProcessContext context = new RegisterProcessContext(client);

		RegisterProcess process = new RegisterProcess(profile, credentials, context);
		AsyncComponent asyncProcess = new AsyncComponent(process);

		ProcessListener listener = new ProcessListener();
		asyncProcess.attachListener(listener);
		asyncProcess.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// TODO verify put user profile
		// UserProfile getUserProfile = ProcessTestUtil.getUserProfile(otherClient, credentials);
		//
		// assertNotNull(getUserProfile);
		// assertEquals(credentials.getUserId(), getUserProfile.getUserId());

		// verify put locations
		FutureGet getLocations = otherClient.getDataManager().get(
				Number160.createHash(credentials.getUserId()), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(H2HConstants.USER_LOCATIONS));
		getLocations.awaitUninterruptibly();
		getLocations.getFutureRequests().awaitUninterruptibly();
		Locations locations = (Locations) getLocations.getData().object();

		assertNotNull(locations);
		assertEquals(credentials.getUserId(), locations.getUserId());
		assertTrue(locations.getPeerAddresses().isEmpty());

		// verify put user public key
		FutureGet getKey = otherClient.getDataManager().get(Number160.createHash(credentials.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_PUBLIC_KEY));
		getKey.awaitUninterruptibly();
		getKey.getFutureRequests().awaitUninterruptibly();
		UserPublicKey publicKey = (UserPublicKey) getKey.getData().object();

		assertNotNull(publicKey);
		assertEquals(profile.getEncryptionKeys().getPublic(), publicKey.getPublicKey());

	}
	
	@Test
	public void testFailOnExistingLocations() throws InvalidProcessStateException {
		
		NetworkManager client = network.get(0);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		UserProfile profile = new UserProfile(credentials.getUserId());

		// already put a locations map
		FuturePut putLocations = client.getDataManager().put(Number160.createHash(credentials.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS),
				new Locations(credentials.getUserId()), null);
		putLocations.awaitUninterruptibly();
		putLocations.getFutureRequests().awaitUninterruptibly();
		
		assertTrue(putLocations.isSuccess());

		RegisterProcessContext context = new RegisterProcessContext(client);

		RegisterProcess process = new RegisterProcess(profile, credentials, context);
//		AsyncComponent asyncProcess = new AsyncComponent(process);

		ProcessListener listener = new ProcessListener();
//		asyncProcess.attachListener(listener);
//		asyncProcess.start();
		process.attachListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}
}
