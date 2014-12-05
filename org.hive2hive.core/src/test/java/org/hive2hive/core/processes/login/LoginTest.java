package org.hive2hive.core.processes.login;

import java.io.IOException;
import java.util.ArrayList;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the login procedure. Should not be able to login if the credentials are wrong.
 * 
 * @author Nico
 */
public class LoginTest extends H2HJUnitTest {

	private static final int networkSize = 10;
	private static ArrayList<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static TestFileAgent fileAgent;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LoginTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = generateRandomCredentials();

		UseCaseTestUtil.register(userCredentials, network.get(0));
		fileAgent = new TestFileAgent();
	}

	@Test
	public void testValidCredentials() throws ClassNotFoundException, IOException, NoSessionException,
			NoPeerConnectionException {
		NetworkManager client = NetworkTestUtil.getRandomNode(network);

		// login with valid credentials
		UseCaseTestUtil.login(userCredentials, client, fileAgent);

		Assert.assertNotNull(client.getSession());
		Assert.assertEquals(userCredentials.getUserId(), client.getUserId());

		// verify the locations map
		Locations locations = (Locations) client.getDataManager().get(
				new Parameters().setLocationKey(userCredentials.getUserId()).setContentKey(H2HConstants.USER_LOCATIONS));
		Assert.assertEquals(1, locations.getPeerAddresses().size());
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidPassword() throws NoSessionException, InvalidProcessStateException, NoPeerConnectionException,
			ProcessExecutionException {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(), randomString(),
				userCredentials.getPin());

		loginAndWaitToFail(wrongCredentials);
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidPin() throws NoSessionException, InvalidProcessStateException, NoPeerConnectionException,
			ProcessExecutionException {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(), userCredentials.getPassword(),
				randomString());

		loginAndWaitToFail(wrongCredentials);
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidUserId() throws NoSessionException, InvalidProcessStateException, NoPeerConnectionException,
			ProcessExecutionException {
		UserCredentials wrongCredentials = new UserCredentials(randomString(), userCredentials.getPassword(),
				userCredentials.getPin());

		loginAndWaitToFail(wrongCredentials);
	}

	public H2HSession loginAndWaitToFail(UserCredentials wrongCredentials) throws InvalidProcessStateException,
			NoSessionException, NoPeerConnectionException {
		NetworkManager client = NetworkTestUtil.getRandomNode(network);
		SessionParameters sessionParameters = new SessionParameters(fileAgent);
		IProcessComponent<Void> loginProcess = ProcessFactory.instance().createLoginProcess(wrongCredentials,
				sessionParameters, client);
		TestExecutionUtil.executeProcessTillFailed(loginProcess);

		return client.getSession();
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
