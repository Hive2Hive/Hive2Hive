package org.hive2hive.core.test.processes.implementations.login;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the login procedure. Should not be able to login if the credentials are wrong.
 * 
 * @author Nico
 * 
 */
public class LoginTest extends H2HJUnitTest {

	private static final int networkSize = 10;
	private static List<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LoginTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		UseCaseTestUtil.register(userCredentials, network.get(0));
		root = FileUtils.getTempDirectory();
	}

	@Test
	public void testValidCredentials() throws ClassNotFoundException, IOException, NoSessionException,
			NoPeerConnectionException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// login with valid credentials
		UseCaseTestUtil.login(userCredentials, client, root);

		Assert.assertNotNull(client.getSession());
		Assert.assertEquals(userCredentials.getUserId(), client.getUserId());

		// verify the locations map
		FutureGet futureGet = client.getDataManager().get(Number160.createHash(userCredentials.getUserId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();

		Locations locations = (Locations) futureGet.getData().object();
		Assert.assertEquals(1, locations.getPeerAddresses().size());
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidPassword() throws NoSessionException, InvalidProcessStateException,
			NoPeerConnectionException {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(),
				NetworkTestUtil.randomString(), userCredentials.getPin());

		loginAndWaitToFail(wrongCredentials);
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidPin() throws NoSessionException, InvalidProcessStateException,
			NoPeerConnectionException {
		UserCredentials wrongCredentials = new UserCredentials(userCredentials.getUserId(),
				userCredentials.getPassword(), NetworkTestUtil.randomString());

		loginAndWaitToFail(wrongCredentials);
	}

	@Test(expected = NoSessionException.class)
	public void testInvalidUserId() throws NoSessionException, InvalidProcessStateException,
			NoPeerConnectionException {
		UserCredentials wrongCredentials = new UserCredentials(NetworkTestUtil.randomString(),
				userCredentials.getPassword(), userCredentials.getPin());

		loginAndWaitToFail(wrongCredentials);
	}

	public H2HSession loginAndWaitToFail(UserCredentials wrongCredentials)
			throws InvalidProcessStateException, NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		SessionParameters sessionParameters = new SessionParameters();
		sessionParameters.setProfileManager(new UserProfileManager(client, wrongCredentials));

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(wrongCredentials,
				sessionParameters, client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		loginProcess.attachListener(listener);
		loginProcess.start();

		UseCaseTestUtil.waitTillFailed(listener, 20);

		return client.getSession();
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
