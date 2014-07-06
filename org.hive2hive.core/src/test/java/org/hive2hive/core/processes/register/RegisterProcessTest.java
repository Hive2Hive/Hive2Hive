package org.hive2hive.core.processes.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.concretes.ProcessComponentListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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

	@Test
	public void testRegisterProcessSuccess() throws InvalidProcessStateException, ClassNotFoundException, IOException,
			GetFailedException, NoPeerConnectionException {
		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.register(credentials, client);

		// verify put user profile
		UserProfile getUserProfile = UseCaseTestUtil.getUserProfile(otherClient, credentials);

		assertNotNull(getUserProfile);
		assertEquals(credentials.getUserId(), getUserProfile.getUserId());

		// verify put locations
		FutureGet getLocations = otherClient.getDataManager().getUnblocked(
				new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_LOCATIONS));
		getLocations.awaitUninterruptibly();
		getLocations.getFutureRequests().awaitUninterruptibly();
		Locations locations = (Locations) getLocations.getData().object();

		assertNotNull(locations);
		assertEquals(credentials.getUserId(), locations.getUserId());
		assertTrue(locations.getPeerAddresses().isEmpty());

		// verify put user public key
		FutureGet getKey = otherClient.getDataManager().getUnblocked(
				new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_PUBLIC_KEY));
		getKey.awaitUninterruptibly();
		getKey.getFutureRequests().awaitUninterruptibly();
		UserPublicKey publicKey = (UserPublicKey) getKey.getData().object();

		assertNotNull(publicKey);
	}

	@Test
	public void testFailOnExistingLocations() throws InvalidProcessStateException, NoPeerConnectionException {
		NetworkManager client = network.get(0);

		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		// already put a locations map
		FuturePut putLocations = client.getDataManager().putUnblocked(
				new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_LOCATIONS)
						.setData(new Locations(credentials.getUserId())));
		putLocations.awaitUninterruptibly();
		putLocations.getFutureRequests().awaitUninterruptibly();

		assertTrue(putLocations.isSuccess());

		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials, client);
		ProcessComponentListener listener = new ProcessComponentListener();
		registerProcess.attachListener(listener);
		registerProcess.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Test
	public void testRegisterMultipleUsers() throws NoPeerConnectionException {
		// register three users on two peers
		UserCredentials credentials1 = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.register(credentials1, network.get(0));

		UserCredentials credentials2 = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.register(credentials2, network.get(1));

		UserCredentials credentials3 = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.register(credentials3, network.get(0));

		// login two of them
		UseCaseTestUtil.login(credentials2, network.get(0), FileTestUtil.getTempDirectory());
		UseCaseTestUtil.login(credentials1, network.get(1), FileTestUtil.getTempDirectory());
	}
}
