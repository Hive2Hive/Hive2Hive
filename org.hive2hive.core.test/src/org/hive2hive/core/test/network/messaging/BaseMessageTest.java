package org.hive2hive.core.test.network.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.network.data.TestDataWrapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseMessageTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

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
	public void testSendingAnAsynchronousMessageWithNoReplyToTargetNode() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String contentKey = NetworkTestUtil.randomString();
		String data = "test data";

		assertNull(nodeB.getLocal(nodeB.getNodeId(), contentKey));

		TestMessageOneWay message = new TestMessageOneWay(nodeB.getNodeId(), contentKey, new TestDataWrapper(
				data));
		nodeA.send(message);

		Waiter w = new Waiter(20);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeB.getLocal(nodeB.getNodeId(), contentKey);
		} while (tmp == null);

		String result = ((TestDataWrapper) tmp).getTestString();
		assertNotNull(result);
		assertEquals(data, result);
	}

	/**
	 * This tests sends a message asynchronously to the target node. The message is configured in a way that
	 * it will be blocked on the target node till the maximum allowed numbers of retrying to send the very
	 * message is reached. At this stage the message is accepted and executed on the target node.
	 */
	@Test
	public void testSendingAnAsynchronousMessageWithNoReplyMaxTimesTargetNode() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String contentKey = NetworkTestUtil.randomString();
		String data = "test data";

		assertNull(nodeB.getLocal(nodeB.getNodeId(), contentKey));

		TestMessageOneWayMaxSending message = new TestMessageOneWayMaxSending(nodeB.getNodeId(), contentKey,
				new TestDataWrapper(data));
		nodeA.send(message);

		Waiter w = new Waiter(20);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeB.getLocal(nodeB.getNodeId(), contentKey);
		} while (tmp == null);

		String result = ((TestDataWrapper) tmp).getTestString();
		assertNotNull(result);
		assertEquals(data, result);
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

	private class Waiter {
		private int counter = 0;
		private final int maxAmoutOfTicks;

		public Waiter(int anAmountOfAcceptableTicks) {
			maxAmoutOfTicks = anAmountOfAcceptableTicks;
		}

		public void tickASecond() {
			synchronized (this) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
				}
			}
			counter++;
			if (counter >= maxAmoutOfTicks) {
				fail(String.format("We waited for %d seconds. This is simply to long!", counter));
			}
		}
	}

}
