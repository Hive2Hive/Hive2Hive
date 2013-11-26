package org.hive2hive.core.test.process.common.massages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Random;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.messages.BaseDirectMessageProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link BaseDirectMessageProcessStep} class. Checks if the process step successes when message
 * successfully arrives and if the process step fails (triggers rollback) when the sending of a message fails.
 * 
 * @author Seppi
 */
public class BaseDirectMessageProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseDirectMessageProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.createKeyPairs(network);
	}

	/**
	 * Sends a direct asynchronous message through a process step. This test checks if the process step
	 * successes
	 * when the message arrives at the right target node (given through {@link PeerAddress}). This
	 * is verified by locally storing and looking for the sent test data at the receiving node.
	 */
	@Test
	public void baseDirectMessageProcessStepTestOnSuccess() {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		// generate random data and content key
		String data = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		// check if selected location is empty
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));

		// create a message with target node B
		TestDirectMessage message = new TestDirectMessage(nodeB.getNodeId(), nodeB.getPeerAddress(),
				contentKey, new H2HTestData(data), false);

		// initialize the process and the one and only step to test
		Process process = new Process(nodeA) {
		};

		BaseDirectMessageProcessStep step = new BaseDirectMessageProcessStep(message, nodeB.getPublicKey(),
				null) {
			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				Assert.fail("Should be not used.");
			}
		};
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey);
		} while (tmp == null);

		// verify that data arrived
		String result = ((H2HTestData) tmp).getTestString();
		assertNotNull(result);
		assertEquals(data, result);
	}

	/**
	 * Sends an asynchronous message through a process step. This test checks if the process step fails
	 * when the message gets denied at the target node (node which is responsible for the given key).
	 */
	@Test
	public void baseDirectMessageProcessStepTestOnFailure() {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		// generate random data and content key
		String data = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		// check if selected location is empty
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));

		// assign a denying message handler at target node
		nodeB.getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());

		// create a message with target node B
		TestDirectMessage message = new TestDirectMessage(nodeB.getNodeId(), nodeB.getPeerAddress(),
				contentKey, new H2HTestData(data), false);

		// initialize the process and the one and only step to test
		Process process = new Process(nodeA) {
		};

		BaseDirectMessageProcessStep step = new BaseDirectMessageProcessStep(message, nodeB.getPublicKey(),
				null) {
			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				Assert.fail("Should be not used.");
			}
		};
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		// check if selected location is still empty
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));
	}

	/**
	 * Sends an asynchronous request message through a process step. This test checks if the process step
	 * successes when receiving node responds to a request message.
	 */
	@Test
	public void baseDirectMessageProcessStepTestWithARequestMessage() {
		// select two random nodes
		final NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		// generate a random content key
		final String contentKey = NetworkTestUtil.randomString();
		// check if selected locations are empty
		assertNull(nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey));
		assertNull(nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey));
		// create a message with target node B
		TestDirectMessageWithReply message = new TestDirectMessageWithReply(nodeB.getPeerAddress(),
				contentKey);

		// initialize the process and the one and only step to test
		Process process = new Process(nodeA) {
		};

		BaseDirectMessageProcessStep step = new BaseDirectMessageProcessStep(message, nodeB.getPublicKey(),
				null) {
			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				// locally store on requesting node received data
				String receivedSecret = (String) responseMessage.getContent();
				nodeA.getDataManager().putLocal(nodeA.getNodeId(), contentKey,
						new H2HTestData(receivedSecret));
				// step finished go further
				getProcess().setNextStep(nextStep);
			}
		};
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// wait till response message gets handled
		waiter = new H2HWaiter(10);
		Object tmp = null;
		do {
			waiter.tickASecond();
			tmp = nodeA.getDataManager().getLocal(nodeA.getNodeId(), contentKey);
		} while (tmp == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) tmp).getTestString();
		String originalSecret = ((H2HTestData) nodeB.getDataManager().getLocal(nodeB.getNodeId(), contentKey))
				.getTestString();

		assertEquals(originalSecret, receivedSecret);
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class DenyingMessageReplyHandler implements ObjectDataReply {
		@Override
		public Object reply(PeerAddress sender, Object request) throws Exception {
			return AcceptanceReply.FAILURE;
		}
	}
}
