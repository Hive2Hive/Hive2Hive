package org.hive2hive.core.test.network.messages;

import static org.junit.Assert.assertFalse;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test to test message signatures.
 * 
 * @author Seppi
 */
public class MessageSignatureTest extends H2HJUnitTest {

	private List<NetworkManager> network;
	private final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseMessageTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testMessageWithSignature() {
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);

		// assign same key pairs to simulate logged in users
		KeyPair keyPair1 = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
		sender.setSession(new H2HSession(keyPair1, null, null, null));
		KeyPair keyPair2 = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
		receiver.setSession(new H2HSession(keyPair2, null, null, null));

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B
		TestSignedMessage message = new TestSignedMessage(locationKey, keyPair1.getPublic());

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			w.tickASecond();
		} while (!listener.hasSucceded());
	}

	@Test
	public void testMessageWithWrongSignature() {
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);

		// assign same key pairs to simulate logged in users
		KeyPair keyPair1 = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
		sender.setSession(new H2HSession(keyPair1, null, null, null));
		KeyPair keyPair2 = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
		receiver.setSession(new H2HSession(keyPair2, null, null, null));

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B, assign random public key
		TestSignedMessage message = new TestSignedMessage(locationKey, EncryptionUtil.generateRSAKeyPair(
				H2HConstants.H2H_RSA_KEYLENGTH).getPublic());

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceded());
			w.tickASecond();
		} while (!listener.hasFailed());
	}

	private class TestMessageVerifyListener implements IBaseMessageListener {

		private boolean failed = false;
		private boolean succeded = false;

		public boolean hasSucceded() {
			return succeded;
		}

		public boolean hasFailed() {
			return failed;
		}

		@Override
		public void onSuccess() {
			succeded = true;
		}

		@Override
		public void onFailure() {
			failed = true;
		}

	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
}
