package org.hive2hive.core.test.network.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.network.data.TestDataWrapper;
import org.hive2hive.core.test.network.messaging.TestMessageWithReply.TestCallBackHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseRequestMessageTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseRequestMessageTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testSendingAnAsynchronousMessageWithReply() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String contentKey = NetworkTestUtil.randomString();
		
		assertNull(nodeA.getLocal(nodeA.getNodeId(), contentKey));
		assertNull(nodeB.getLocal(nodeB.getNodeId(), contentKey));

		TestMessageWithReply message = new TestMessageWithReply(nodeB.getNodeId(), nodeA.getPeerAddress(), contentKey);
		TestCallBackHandler callBackHandler = message.new TestCallBackHandler(nodeA);
		message.setCallBackHandler(callBackHandler);
		nodeA.send(message);

		Waiter w = new Waiter(10);
		Object tmp = null;
		do {
			w.tickASecond();
			tmp = nodeA.getLocal(nodeA.getNodeId(), contentKey);
		} while (tmp == null);
		
		String receivedSecret = (String) ((TestDataWrapper) tmp).getContent();
		String originalSecret =  (String) ((TestDataWrapper) nodeB.getLocal(nodeB.getNodeId(), contentKey)).getContent();
		
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
