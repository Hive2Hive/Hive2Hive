package org.hive2hive.core.test.network.messages.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.network.messages.TestBaseMessageListener;
import org.hive2hive.core.test.network.messages.direct.TestDirectMessageWithReply.TestCallBackHandler;
import org.hive2hive.core.test.network.messages.direct.TestDirectMessageWithReplyMaxSending.TestCallBackHandlerMaxSendig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.createKeyPairs(network);
	}

	@Test
	public void testSendingDirectMessageWithRequest() {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		// generate a random content key
		String contentKey = NetworkTestUtil.randomString();
		// check if selected locations are empty
		assertNull(nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey));
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));
		// create a message with target node B
		TestDirectMessageWithReply message = new TestDirectMessageWithReply(nodeB.getPeerAddress(),
				contentKey);
		// create and add a callback handler
		TestCallBackHandler callBackHandler = message.new TestCallBackHandler(nodeA);
		message.setCallBackHandler(callBackHandler);
		// send message
		nodeA.sendDirect(message, nodeB.getKeyPair().getPublic(), new TestBaseMessageListener());

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey);
		} while (tmp == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) tmp).getTestString();
		String originalSecret = ((H2HTestData) nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey)).getTestString();

		assertEquals(originalSecret, receivedSecret);
	}

	@Test
	public void testSendingDirectMessageWithNoReplyMaxTimesRequestingNode() {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		// generate a random content key
		String contentKey = NetworkTestUtil.randomString();
		// check if selected locations are empty
		assertNull(nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey));
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));
		// create a message with target node B
		TestDirectMessageWithReplyMaxSending message = new TestDirectMessageWithReplyMaxSending(
				nodeB.getPeerAddress(), contentKey);
		// create and add a callback handler
		TestCallBackHandlerMaxSendig callBackHandler = message.new TestCallBackHandlerMaxSendig(nodeA);
		message.setCallBackHandler(callBackHandler);
		// send message
		nodeA.sendDirect(message, nodeB.getKeyPair().getPublic(), new TestBaseMessageListener());

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey);
		} while (tmp == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) tmp).getTestString();
		String originalSecret = ((H2HTestData) nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey)).getTestString();

		assertEquals(originalSecret, receivedSecret);
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
