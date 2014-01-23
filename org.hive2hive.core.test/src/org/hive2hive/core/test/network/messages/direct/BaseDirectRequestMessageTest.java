package org.hive2hive.core.test.network.messages.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.network.messages.direct.TestDirectMessageWithReply.TestCallBackHandler;
import org.hive2hive.core.test.network.messages.direct.TestDirectMessageWithReplyMaxSending.TestCallBackHandlerMaxSendig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests asynchronous direct request messages where the receiver node has to respond with a response message
 * which gets handled by a callback handler.
 * 
 * @author Seppi
 */
public class BaseDirectRequestMessageTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseDirectRequestMessageTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.createSameKeyPair(network);
	}

	@Test
	public void testSendingDirectMessageWithRequest() throws ClassNotFoundException, IOException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		// generate a random content key
		String contentKey = NetworkTestUtil.randomString();
		// create a message with target node B
		TestDirectMessageWithReply message = new TestDirectMessageWithReply(nodeB.getPeerAddress(),
				contentKey);
		// create and add a callback handler
		TestCallBackHandler callBackHandler = message.new TestCallBackHandler(nodeA);
		message.setCallBackHandler(callBackHandler);

		// send message
		assertTrue(nodeA.sendDirect(message, nodeB.getPublicKey()));

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		FutureGet futureGet = null;
		do {
			w.tickASecond();
			futureGet = nodeB.getDataManager().get(Number160.createHash(nodeA.getNodeId()),
					H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) futureGet.getData().object()).getTestString();
		futureGet = nodeB.getDataManager().get(Number160.createHash(nodeB.getNodeId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		String originalSecret = ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(originalSecret, receivedSecret);
	}

	@Test
	public void testSendingDirectMessageWithNoReplyMaxTimesRequestingNode() throws ClassNotFoundException,
			IOException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		// generate a random content key
		String contentKey = NetworkTestUtil.randomString();
		// create a message with target node B
		TestDirectMessageWithReplyMaxSending message = new TestDirectMessageWithReplyMaxSending(
				nodeB.getPeerAddress(), contentKey);
		// create and add a callback handler
		TestCallBackHandlerMaxSendig callBackHandler = message.new TestCallBackHandlerMaxSendig(nodeA);
		message.setCallBackHandler(callBackHandler);

		// send message
		assertTrue(nodeA.sendDirect(message, nodeB.getPublicKey()));

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		FutureGet futureGet = null;
		do {
			w.tickASecond();
			futureGet = nodeB.getDataManager().get(Number160.createHash(nodeA.getNodeId()),
					H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) futureGet.getData().object()).getTestString();
		futureGet = nodeB.getDataManager().get(Number160.createHash(nodeB.getNodeId()),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		String originalSecret = ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(originalSecret, receivedSecret);
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
