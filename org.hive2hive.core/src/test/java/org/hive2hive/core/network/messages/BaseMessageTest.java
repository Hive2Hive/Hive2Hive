package org.hive2hive.core.network.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.testmessages.TestMessage;
import org.hive2hive.core.network.messages.testmessages.TestMessageMaxSending;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class BaseMessageTest extends H2HJUnitTest {

	private static ArrayList<NetworkManager> network;
	private final static int networkSize = 10;
	private Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseMessageTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.setSameSession(network);
	}

	/**
	 * This test checks if a message arrives asynchronously at the right target node (node which is
	 * responsible for the given key). To verify this the receiving node stores the received data into the
	 * DHT. Everything went right when same data is found with the node id of the receiving node as location
	 * key.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void testSendingAnAsynchronousMessageWithNoReplyToTargetNode() throws ClassNotFoundException, IOException,
			NoPeerConnectionException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		// generate random data and content key
		String data = randomString();
		String contentKey = randomString();
		// create a message with target node B
		TestMessage message = new TestMessage(nodeB.getNodeId(), contentKey, new H2HTestData(data));

		// send message
		assertTrue(nodeA.getMessageManager().send(message, getPublicKey(nodeB)));

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		BaseNetworkContent content = null;
		do {
			w.tickASecond();
			content = nodeB.getDataManager().get(
					new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey));
		} while (content == null);

		// verify that data arrived
		String result = ((H2HTestData) content).getTestString();
		assertNotNull(result);
		assertEquals(data, result);
	}

	/**
	 * This tests sends a message asynchronously to the target node. The message is configured in a way that
	 * it will be blocked on the target node till the maximum allowed numbers of retrying to send the very
	 * message is reached. At this stage the message is accepted and executed on the target node.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	@Test
	public void testSendingAnAsynchronousMessageWithNoReplyMaxTimesTargetNode() throws ClassNotFoundException, IOException,
			NoPeerConnectionException, NoSessionException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		// receiver node should already know the public key of the sender
		nodeB.getSession().getKeyManager().putPublicKey(nodeA.getUserId(), getPublicKey(nodeA));

		// generate random data and content key
		String contentKey = randomString();
		String data = randomString();
		// create a test message which gets rejected several times
		TestMessageMaxSending message = new TestMessageMaxSending(nodeB.getNodeId(), contentKey, new H2HTestData(data));

		// send message
		assertTrue(nodeA.getMessageManager().send(message, getPublicKey(nodeB)));

		// wait till message gets handled
		// this might need some time
		H2HWaiter w = new H2HWaiter(10);
		BaseNetworkContent content = null;
		do {
			w.tickASecond();
			content = nodeB.getDataManager().get(
					new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey));
		} while (content == null);

		// verify that data arrived
		String result = ((H2HTestData) content).getTestString();
		assertNotNull(result);
		assertEquals(data, result);
	}

	private PublicKey getPublicKey(NetworkManager networkManager) {
		try {
			return networkManager.getSession().getKeyPair().getPublic();
		} catch (NoSessionException e) {
			return null;
		}
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
