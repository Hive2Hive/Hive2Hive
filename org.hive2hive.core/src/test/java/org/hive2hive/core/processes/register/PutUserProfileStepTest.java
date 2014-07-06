package org.hive2hive.core.processes.register;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.common.base.DenyingPutTestStorage;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.core.processes.register.PutUserProfileStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the user profile into the DHT.
 * 
 * @author Nico, Seppi
 */
public class PutUserProfileStepTest extends H2HJUnitTest {

	private static final int networkSize = 2;
	private static List<NetworkManager> network;
	private static IH2HEncryption encryption;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutUserProfileStepTest.class;
		beforeClass();
		encryption = new H2HDummyEncryption();
		network = NetworkTestUtil.createNetwork(networkSize, encryption);
	}

	@Test
	public void testStepSuccessful() throws InterruptedException, InvalidKeySpecException, DataLengthException,
			IllegalStateException, InvalidCipherTextException, ClassNotFoundException, IOException,
			NoPeerConnectionException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager client = network.get(1); // where the user profile is stored

		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		UserProfile testProfile = new UserProfile(credentials.getUserId());
		RegisterProcessContext context = new RegisterProcessContext(credentials);
		context.provideUserProfile(testProfile);

		// initialize the process and the one and only step to test
		PutUserProfileStep step = new PutUserProfileStep(context, putter.getDataManager());
		TestExecutionUtil.executeProcess(step);

		// get the user profile which should be stored at the proxy
		FutureGet global = client.getDataManager().getUnblocked(
				new Parameters().setLocationKey(credentials.getProfileLocationKey())
						.setContentKey(H2HConstants.USER_PROFILE));
		global.awaitUninterruptibly();
		global.getFutureRequests().awaitUninterruptibly();
		EncryptedNetworkContent found = (EncryptedNetworkContent) global.getData().object();
		Assert.assertNotNull(found);

		// decrypt it using the same password as set above
		SecretKey decryptionKeys = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(), credentials.getPin(),
				H2HConstants.KEYLENGTH_USER_PROFILE);
		UserProfile decrypted = (UserProfile) encryption.decryptAES(found, decryptionKeys);

		// verify if both objects are the same
		Assert.assertEquals(credentials.getUserId(), decrypted.getUserId());
	}

	@Test
	public void testStepRollback() throws InterruptedException, NoPeerConnectionException, InvalidProcessStateException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		// create the needed objects
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		UserProfile testProfile = new UserProfile(credentials.getUserId());
		RegisterProcessContext context = new RegisterProcessContext(credentials);
		context.provideUserProfile(testProfile);

		// initialize the process and the one and only step to test
		PutUserProfileStep step = new PutUserProfileStep(context, putter.getDataManager());
		TestProcessComponentListener listener = new TestProcessComponentListener();
		step.attachListener(listener);
		step.start();

		TestExecutionUtil.waitTillFailed(listener, 20);

		// get the locations which should be stored at the proxy --> they should be null
		FutureGet futureGet = proxy.getDataManager().getUnblocked(
				new Parameters().setLocationKey(credentials.getProfileLocationKey()).setContentKey(
						H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
