package org.hive2hive.core.test.process;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
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

	enum StepAction {
		PUT,
		GET,
		REMOVAL
	};

	private final static int networkSize = 2;
	private static List<NetworkManager> network;
	private Map<String, ResponseMessage> messageWaiterMap;
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

		MessageProcessStep step = new MessageProcessStep(sender, receiver, testContent);
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
	public void testPut() throws InterruptedException, IOException {
		final String contentKey = "TEST";
		final H2HTestData data = new H2HTestData(testContent);
		final NetworkManager putter = network.get(0);
		final NetworkManager holder = network.get(1);

		PutGetRemovalProcessStep step = new PutGetRemovalProcessStep(holder.getNodeId(), contentKey, data,
				StepAction.PUT);
		Process process = new Process(putter) {
		};
		process.setFirstStep(step);

		// check that receiver does not have any content
		Assert.assertNull(holder.getLocal(contentKey, contentKey));

		process.start();
		FuturePut future = (FuturePut) waitForFutureResult();
		Assert.assertTrue(future.isSuccess());
		Assert.assertTrue(future.isCompleted());

		// now, the receiver should have the content in memory
		H2HTestData received = (H2HTestData) holder.getLocal(holder.getNodeId(), contentKey);
		Assert.assertNotNull(received);

		Assert.assertEquals(testContent, (String) received.getTestString());
	}

	@Test
	public void testGet() throws InterruptedException, IOException, ClassNotFoundException {
		final String contentKey = "TEST";
		final H2HTestData data = new H2HTestData(testContent);
		final NetworkManager getter = network.get(0);
		final NetworkManager holder = network.get(1);

		// put in the memory of 2nd peer
		holder.putLocal(holder.getNodeId(), contentKey, data);

		PutGetRemovalProcessStep step = new PutGetRemovalProcessStep(holder.getNodeId(), contentKey, data,
				StepAction.GET);
		Process process = new Process(getter) {
		};
		process.setFirstStep(step);

		// check that receiver does not have any content
		Assert.assertNull(holder.getLocal(contentKey, contentKey));

		process.start();
		FutureGet future = (FutureGet) waitForFutureResult();
		Assert.assertTrue(future.isSuccess());
		Assert.assertTrue(future.isCompleted());

		// now, the receiver should have the content in memory
		H2HTestData received = (H2HTestData) future.getData().object();
		Assert.assertEquals(testContent, (String) received.getTestString());
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
		PutGetRemovalProcessStep step = new PutGetRemovalProcessStep(holder.getNodeId(), contentKey, data,
				StepAction.REMOVAL);
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
	private class MessageProcessStep extends ProcessStep {

		private final ProcessStepTestMessage messageToSend;

		public MessageProcessStep(NetworkManager sender, NetworkManager receiver, String testContent) {
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

		@Override
		protected void handleGetResult(FutureGet future) {
			// not expected to get a put/get result
			Assert.fail();
		}

		@Override
		protected void handlePutResult(FuturePut future) {
			// not expected to get a put/get result
			Assert.fail();
		}

		@Override
		protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
			// notify the message waiter
			synchronized (messageWaiterMap) {
				messageWaiterMap.put(asyncReturnMessage.getMessageID(), asyncReturnMessage);
			}
			getProcess().nextStep(null);
		}

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
	private class PutGetRemovalProcessStep extends ProcessStep {

		private NetworkContent data;
		private String locationKey;
		private String contentKey;
		private StepAction action;

		/**
		 * 
		 * @param locationKey
		 * @param contentKey
		 * @param data
		 * @param put if true, then the step puts the data, else it gets the data from the location/content
		 *            key
		 */
		public PutGetRemovalProcessStep(String locationKey, String contentKey, NetworkContent data,
				StepAction action) {
			this.locationKey = locationKey;
			this.contentKey = contentKey;
			this.data = data;
			this.action = action;
		}

		@Override
		public void start() {
			switch (action) {
				case PUT:
					put(locationKey, contentKey, data);
					break;
				case GET:
					get(locationKey, contentKey);
					break;
				case REMOVAL:
					remove(locationKey, contentKey);
					break;
				default:
					break;
			}
		}

		@Override
		public void rollBack() {
			Assert.fail("Should not have rollbacked here");
		}

		@Override
		protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
			// not expected to get a message reply
			Assert.fail();
		}

		@Override
		protected void handlePutResult(FuturePut future) {
			// notify the message waiter
			synchronized (messageWaiterMap) {
				tempFutureStore = future;
			}
			getProcess().nextStep(null);
		}

		@Override
		protected void handleGetResult(FutureGet future) {
			// notify the message waiter
			synchronized (messageWaiterMap) {
				tempFutureStore = future;
			}
			getProcess().nextStep(null);
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
