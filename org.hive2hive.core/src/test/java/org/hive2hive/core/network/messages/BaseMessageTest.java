package org.hive2hive.core.network.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseMessageTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private final static int networkSize = 5;
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
	public void testSendingAnAsynchronousMessageWithNoReplyToTargetNode() throws ClassNotFoundException,
			IOException, NoPeerConnectionException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		// generate random data and content key
		String data = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		// create a message with target node B
		TestMessage message = new TestMessage(nodeB.getNodeId(), contentKey, new H2HTestData(data));

		// send message
		assertTrue(nodeA.getMessageManager().send(message, getPublicKey(nodeB)));

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		FutureGet futureGet = null;
		do {
			w.tickASecond();
			futureGet = nodeB.getDataManager().getUnblocked(
					new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey));
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// verify that data arrived
		String result = ((H2HTestData) futureGet.getData().object()).getTestString();
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
	public void testSendingAnAsynchronousMessageWithNoReplyMaxTimesTargetNode()
			throws ClassNotFoundException, IOException, NoPeerConnectionException, NoSessionException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		// receiver node should already know the public key of the sender
		nodeB.getSession().getKeyManager().putPublicKey(nodeA.getUserId(), getPublicKey(nodeA));

		// generate random data and content key
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();
		// create a test message which gets rejected several times
		TestMessageMaxSending message = new TestMessageMaxSending(nodeB.getNodeId(), contentKey,
				new H2HTestData(data));

		// send message
		assertTrue(nodeA.getMessageManager().send(message, getPublicKey(nodeB)));

		// wait till message gets handled
		// this might need some time
		H2HWaiter w = new H2HWaiter(10);
		FutureGet futureGet = null;
		do {
			w.tickASecond();
			futureGet = nodeB.getDataManager().getUnblocked(
					new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey));
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// verify that data arrived
		String result = ((H2HTestData) futureGet.getData().object()).getTestString();
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
