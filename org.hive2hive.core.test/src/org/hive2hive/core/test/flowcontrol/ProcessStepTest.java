package org.hive2hive.core.test.flowcontrol;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessStepTest extends H2HJUnitTest {
	private final static int networkSize = 2;
	private static List<NetworkManager> network;
	private Map<String, ResponseMessage> messageWaiterMap;
	private String testContent;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void setup() {
		messageWaiterMap = new HashMap<String, ResponseMessage>();
		testContent = NetworkTestUtil.randomString();
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private ResponseMessage waitForMessageResponse(final String messageID) throws InterruptedException {
		int counter = 0;
		while (true) {
			synchronized (messageWaiterMap) {
				if (messageWaiterMap.containsKey(messageID)) {
					return messageWaiterMap.get(messageID);
				}
			}

			synchronized (this) {
				wait(500);
			}

			counter++;
			if (counter > 40) {
				Assert.fail("We waited for more than 20 seconds but list wasn't updated");
			}
		}
	}

	@Test
	public void testSendMessage() throws InterruptedException, IOException {
		final NetworkManager sender = network.get(0);
		final NetworkManager receiver = network.get(1);

		DummyProcessStep step = new DummyProcessStep(sender, receiver, testContent);
		Process process = new Process(sender) {
		};
		process.setFirstStep(step);

		String messageId = new String(step.getMessageId()); // copy

		process.start();
		ResponseMessage response = waitForMessageResponse(messageId);

		Assert.assertNotNull(response);
		Assert.assertEquals(testContent, (String) response.getContent());
	}

	/**
	 * A dummy process step that sends a message and waits for a reply
	 */
	private class DummyProcessStep extends ProcessStep {

		private final ProcessStepTestMessage messageToSend;

		public DummyProcessStep(NetworkManager sender, NetworkManager receiver, String testContent) {
			// initialize message here in order to have the message id already ready
			messageToSend = new ProcessStepTestMessage(receiver.getNodeId(), sender.getPeerAddress(),
					sender.getNodeId(), receiver.getPeerAddress(), testContent);
		}

		@Override
		public void start() {
			send(messageToSend);
		}

		@Override
		public void rollBack() {
			Assert.fail("Should not have rollbacked here");
		}

		@Override
		protected void handleGetResult(FutureDHT future) {
			// it's a test for messages only
		}
		
		@Override
		protected void handlePutResult(FutureDHT future) {
			// it's a test for messages only
		}

		@Override
		protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
			// notify the message waiter
			synchronized (messageWaiterMap) {
				messageWaiterMap.put(asyncReturnMessage.getMessageID(), asyncReturnMessage);
			}
		}

		public String getMessageId() {
			return messageToSend.getMessageID();
		}
	}
}
