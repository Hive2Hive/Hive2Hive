package org.hive2hive.core.test.process;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.MessageProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
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
	@SuppressWarnings("rawtypes")
	private FutureDHT tempFutureStore;
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
		tempFutureStore = null;
		testContent = NetworkTestUtil.randomString();
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private ResponseMessage waitForMessageResponse(final String messageID) throws InterruptedException {
		H2HWaiter w = new H2HWaiter(20);
		ResponseMessage response = null;
		do {
			w.tickASecond();
			synchronized (messageWaiterMap) {
				response = messageWaiterMap.get(messageID);
			}
		} while (response == null);
		return response;
	}

	@SuppressWarnings("rawtypes")
	private FutureDHT waitForFutureResult() throws InterruptedException {
		H2HWaiter w = new H2HWaiter(20);
		FutureDHT response = null;
		do {
			w.tickASecond();
			synchronized (messageWaiterMap) {
				response = tempFutureStore;
			}
		} while (response == null);
		return response;
	}

	@Test
	public void testSendMessage() throws InterruptedException, IOException {
		final NetworkManager sender = network.get(0);
		final NetworkManager receiver = network.get(1);

		DummyMessageProcessStep step = new DummyMessageProcessStep(sender, receiver, testContent);
		Process process = new Process(sender) {
		};
		process.setFirstStep(step);

		String messageId = new String(step.getMessageId()); // copy

		process.start();
		ResponseMessage response = waitForMessageResponse(messageId);

		Assert.assertNotNull(response);
		Assert.assertEquals(testContent, (String) response.getContent());
	}

	@Test
	public void testRemoval() throws InterruptedException {
		final String contentKey = "TEST";
		final H2HTestData data = new H2HTestData(testContent);
		final NetworkManager getter = network.get(0);
		final NetworkManager holder = network.get(1);

		// put the content first
		holder.putLocal(holder.getNodeId(), contentKey, data);
		Assert.assertNotNull(holder.getLocal(holder.getNodeId(), contentKey));

		// start the process which removes the content
		RemovalProcessStep step = new RemovalProcessStep(holder.getNodeId(), contentKey);
		Process process = new Process(getter) {
		};
		process.setFirstStep(step);

		process.start();
		FutureRemove future = (FutureRemove) waitForFutureResult();
		Assert.assertTrue(future.isSuccess());
		Assert.assertTrue(future.isCompleted());

		// the content should be deleted now
		Assert.assertNull(holder.getLocal(holder.getNodeId(), contentKey));
	}

	/**
	 * A dummy process step that sends a message and waits for a reply
	 */
	private class DummyMessageProcessStep extends MessageProcessStep {

		private final ProcessStepTestMessage messageToSend;

		public DummyMessageProcessStep(NetworkManager sender, NetworkManager receiver, String testContent) {
			// initialize message here in order to have the message id already ready
			messageToSend = new ProcessStepTestMessage(receiver.getNodeId(), sender.getPeerAddress(),
					sender.getNodeId(), testContent);
		}

		@Override
		public void start() {
			send(messageToSend);
		}

		@Override
		public void rollBack() {
			Assert.fail("Should not have rollbacked here");
		}

		// @Override
		// protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// // notify the message waiter
		// synchronized (messageWaiterMap) {
		// messageWaiterMap.put(asyncReturnMessage.getMessageID(), asyncReturnMessage);
		// }
		// getProcess().nextStep(null);
		// }

		public String getMessageId() {
			return messageToSend.getMessageID();
		}

		@Override
		protected void handleRemovalResult(FutureRemove future) {
			// not expected to get a removal result
			Assert.fail();
		}
	}

	/**
	 * A dummy process step that puts or gets an object
	 */
	private class RemovalProcessStep extends ProcessStep {

		private String locationKey;
		private String contentKey;

		/**
		 * 
		 * @param locationKey
		 * @param contentKey
		 * @param put if true, then the step puts the data, else it gets the data from the location/content
		 *            key
		 */
		public RemovalProcessStep(String locationKey, String contentKey) {
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		public void start() {
			remove(locationKey, contentKey);
		}

		@Override
		public void rollBack() {
			Assert.fail("Should not have rollbacked here");
		}

		@Override
		protected void handleRemovalResult(FutureRemove future) {
			synchronized (messageWaiterMap) {
				tempFutureStore = future;
			}
			getProcess().nextStep(null);
		}
	}
}
