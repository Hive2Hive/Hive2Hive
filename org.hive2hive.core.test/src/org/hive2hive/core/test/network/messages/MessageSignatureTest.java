package org.hive2hive.core.test.network.messages;

import static org.junit.Assert.assertFalse;

import java.util.List;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test to test message signatures.
 * 
 * @author Seppi
 */
public class MessageSignatureTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseMessageTest.class;
		beforeClass();
	}

	@Test
	public void testMessageWithSignatureSameUser() {
		List<NetworkManager> network = NetworkTestUtil.createNetwork(2);
		NetworkTestUtil.createSameKeyPair(network);
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);
		
		// putting of the public key is not necessary

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B
		TestSignedMessage message = new TestSignedMessage(locationKey);

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			w.tickASecond();
		} while (!listener.hasSucceded());

		NetworkTestUtil.shutdownNetwork(network);
	}

	@Test
	public void testMessageWithSignatureDifferentUser() {
		List<NetworkManager> network = NetworkTestUtil.createNetwork(2);
		NetworkTestUtil.createKeyPairs(network);
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);

		// put the public key of the sender into the network
		sender.getDataManager()
				.put(Number160.createHash(sender.getUserId()), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(H2HConstants.USER_PUBLIC_KEY),
						new UserPublicKey(sender.getPublicKey()), null).awaitUninterruptibly();

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B
		TestSignedMessage message = new TestSignedMessage(locationKey);

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			w.tickASecond();
		} while (!listener.hasSucceded());

		NetworkTestUtil.shutdownNetwork(network);
	}

	@Test
	public void testMessageWithWrongSignature1() {
		List<NetworkManager> network = NetworkTestUtil.createNetwork(2);
		NetworkTestUtil.createKeyPairs(network);
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);

		// don't upload the sender public key

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B, assign random public key
		TestSignedMessage message = new TestSignedMessage(locationKey);

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceded());
			w.tickASecond();
		} while (!listener.hasFailed());

		NetworkTestUtil.shutdownNetwork(network);
	}

	@Test
	public void testMessageWithWrongSignature2() {
		List<NetworkManager> network = NetworkTestUtil.createNetwork(2);
		NetworkTestUtil.createKeyPairs(network);
		NetworkManager sender = network.get(0);
		NetworkManager receiver = network.get(1);

		// put a wrong public key of the sender into the network
		sender.getDataManager()
				.put(Number160.createHash(sender.getUserId()),
						H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(H2HConstants.USER_PUBLIC_KEY),
						new UserPublicKey(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS)
								.getPublic()), null).awaitUninterruptibly();

		// location key is target node id
		String locationKey = receiver.getNodeId();

		// create a message with target node B, assign random public key
		TestSignedMessage message = new TestSignedMessage(locationKey);

		// send message
		TestMessageVerifyListener listener = new TestMessageVerifyListener();
		sender.send(message, receiver.getPublicKey(), listener);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceded());
			w.tickASecond();
		} while (!listener.hasFailed());

		NetworkTestUtil.shutdownNetwork(network);
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

	@AfterClass
	public static void endTest() {
		afterClass();
	}
}
