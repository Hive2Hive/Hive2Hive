package org.hive2hive.core.processes.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegisterProcessTest extends H2HJUnitTest {

	private static List<NetworkManager> network;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RegisterProcessTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(DEFAULT_NETWORK_SIZE);
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	@Test
	public void testRegisterProcessSuccess() throws InvalidProcessStateException, ClassNotFoundException, IOException,
			GetFailedException, NoPeerConnectionException {
		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);

		UserCredentials credentials = generateRandomCredentials();
		UseCaseTestUtil.register(credentials, client);

		// verify put user profile
		UserProfile getUserProfile = UseCaseTestUtil.getUserProfile(otherClient, credentials);

		assertNotNull(getUserProfile);
		assertEquals(credentials.getUserId(), getUserProfile.getUserId());

		// verify put locations
		Locations locations = (Locations) otherClient.getDataManager().get(
				new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_LOCATIONS));
		assertNotNull(locations);
		assertEquals(credentials.getUserId(), locations.getUserId());
		assertTrue(locations.getPeerAddresses().isEmpty());

		// verify put user public key
		UserPublicKey publicKey = (UserPublicKey) otherClient.getDataManager().get(
				new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_PUBLIC_KEY));
		assertNotNull(publicKey);
	}

	@Test
	public void testFailOnExistingLocations() throws InvalidProcessStateException, NoPeerConnectionException {
		NetworkManager client = network.get(0);

		UserCredentials credentials = generateRandomCredentials();

		// already put a locations map
		assertEquals(
				H2HPutStatus.OK,
				client.getDataManager().put(
						new Parameters().setLocationKey(credentials.getUserId()).setContentKey(H2HConstants.USER_LOCATIONS)
								.setNetworkContent(new Locations(credentials.getUserId()))));

		IProcessComponent<Void> registerProcess = ProcessFactory.instance().createRegisterProcess(credentials, client);
		TestExecutionUtil.executeProcessTillFailed(registerProcess);
	}

}
