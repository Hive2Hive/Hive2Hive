package org.hive2hive.core.network.messages.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.direct.testmessages.TestDirectMessageWithReply;
import org.hive2hive.core.network.messages.direct.testmessages.TestDirectMessageWithReply.TestCallBackHandler;
import org.hive2hive.core.network.messages.direct.testmessages.TestDirectMessageWithReplyMaxSending;
import org.hive2hive.core.network.messages.direct.testmessages.TestDirectMessageWithReplyMaxSending.TestCallBackHandlerMaxSendig;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.processframework.util.H2HWaiter;
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

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseDirectRequestMessageTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.setSameSession(network);
	}

	@Test
	public void testSendingDirectMessageWithRequest() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		// generate a random content key
		String contentKey = randomString();
		// create a message with target node B
		TestDirectMessageWithReply message = new TestDirectMessageWithReply(
				nodeB.getConnection().getPeerDHT().peerAddress(), contentKey);
		// create and add a callback handler
		TestCallBackHandler callBackHandler = message.new TestCallBackHandler(nodeA);
		message.setCallBackHandler(callBackHandler);

		// send message
		assertTrue(nodeA.getMessageManager().sendDirect(message, getPublicKey(nodeB)));

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		BaseNetworkContent content = null;
		do {
			w.tickASecond();
			content = nodeB.getDataManager().get(
					new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(contentKey));
		} while (content == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) content).getTestString();
		String originalSecret = ((H2HTestData) nodeB.getDataManager().get(
				new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey))).getTestString();
		assertEquals(originalSecret, receivedSecret);
	}

	@Test
	public void testSendingDirectMessageWithNoReplyMaxTimesRequestingNode() throws ClassNotFoundException, IOException,
			NoPeerConnectionException, NoSessionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		// receiver nodes should already know the public key of the senders
		nodeA.getSession().getKeyManager().putPublicKey(nodeB.getUserId(), getPublicKey(nodeB));
		nodeB.getSession().getKeyManager().putPublicKey(nodeA.getUserId(), getPublicKey(nodeA));

		// generate a random content key
		String contentKey = randomString();
		// create a message with target node B
		TestDirectMessageWithReplyMaxSending message = new TestDirectMessageWithReplyMaxSending(nodeB.getConnection()
				.getPeerDHT().peerAddress(), contentKey);
		// create and add a callback handler
		TestCallBackHandlerMaxSendig callBackHandler = message.new TestCallBackHandlerMaxSendig(nodeA);
		message.setCallBackHandler(callBackHandler);

		// send message
		assertTrue(nodeA.getMessageManager().sendDirect(message, getPublicKey(nodeB)));

		// wait till callback handler gets executed
		H2HWaiter w = new H2HWaiter(10);
		BaseNetworkContent content = null;
		do {
			w.tickASecond();
			content = nodeB.getDataManager().get(
					new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(contentKey));
		} while (content == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) content).getTestString();
		String originalSecret = ((H2HTestData) nodeB.getDataManager().get(
				new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey))).getTestString();
		assertEquals(originalSecret, receivedSecret);
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
