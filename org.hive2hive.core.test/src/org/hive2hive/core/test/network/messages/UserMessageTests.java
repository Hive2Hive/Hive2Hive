package org.hive2hive.core.test.network.messages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.usermessages.ContactPeerUserMessage;
import org.hive2hive.core.network.messages.usermessages.GetNextFromQueueMessage;
import org.hive2hive.core.network.messages.usermessages.GetNextFromQueueMessage.NextFromQueueResponse;
import org.hive2hive.core.network.messages.usermessages.UserMessage;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserMessageTests extends H2HJUnitTest {

	private List<NetworkManager> network;
	private final Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserMessageTests.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(10);
	}

	@Override
	@After
	public void afterMethod() {
		super.afterMethod();
		NetworkTestUtil.shutdownNetwork(network);
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	private static boolean contactPeerUserMessageHandled;
	private static boolean getNextFromQueueMessageHandled;

	@Test
	public void ContactPeerUserMessageTest() {

		// select two random nodes
		NetworkManager node1 = network.get(random.nextInt(network.size()));
		NetworkManager node2 = network.get(random.nextInt(network.size()));

		// create message
		final String evidence = NetworkTestUtil.randomString();
		ContactPeerUserMessage message = new ContactPeerUserMessage(node1.getPeerAddress(),
				node2.getPeerAddress(), evidence);
		message.setCallBackHandler(new IResponseCallBackHandler() {
			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				// handle callback
				contactPeerUserMessageHandled = true;
				logger.debug("ContactPeerUserMessage got handled.");

				String responseEvidence = (String) responseMessage.getContent();
				assertNotNull(responseEvidence);
				assertTrue(evidence.equals(responseEvidence));
			}
		});

		// send message
		node1.sendDirect(message).addListener(new FutureDirectListener(new IBaseMessageListener() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				fail("The sending of the message failed.");
			}
		}, message, node1));

		// wait for callback handling
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!contactPeerUserMessageHandled);
	}

	@Test
	public void GetNextFromQueueMessageTest() {

		// TODO the request to the proxy actually should not happen this way, since the PeerAddress is unknown

		// select two random nodes
		NetworkManager user = network.get(random.nextInt(network.size()));
		NetworkManager proxy = network.get(random.nextInt(network.size()));

		// define a random user
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		String userLocationKey = "mama";//UserProfile.getLocationKey(credentials);

		// prepare and put a sample UserMessageQueue for the user from node2
		UserMessageQueue umq = new UserMessageQueue(credentials.getUserId());
		for (int i = 0; i < 10; i++) {
			umq.getMessageQueue().add(new UserMessage(user.getPeerAddress()) {

				private static final long serialVersionUID = -2306638328974008903L;

				@Override
				public void run() {
					// do nothing
				}
			});
		}
		proxy.putLocal(userLocationKey, H2HConstants.USER_MESSAGE_QUEUE_KEY, umq);

		// wait for proxy to put
		H2HWaiter putWaiter = new H2HWaiter(60);
		NetworkContent tmp = null;
		do {
			putWaiter.tickASecond();
			tmp = proxy.getLocal(userLocationKey, H2HConstants.USER_MESSAGE_QUEUE_KEY);
		} while (tmp == null);
		
		// request the UserMessageQueue
		GetNextFromQueueMessage message = new GetNextFromQueueMessage(user.getPeerAddress(),
				proxy.getPeerAddress(), userLocationKey);
		message.setCallBackHandler(new IResponseCallBackHandler() {
			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				// handle callback
				getNextFromQueueMessageHandled = true;
				logger.debug("GetNextFromQueueMessage got handled.");

				NextFromQueueResponse response = (NextFromQueueResponse) responseMessage.getContent();
				assertNotNull(response);
				assertNotNull(response.getUserMessage());
				assertNotNull(response.getRemainingCount());
				assertTrue(response.getRemainingCount() == 9);
			}
		});

		user.sendDirect(message).addListener(new FutureDirectListener(new IBaseMessageListener() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				fail("The sending of the message failed.");
			}
		}, message, user));

		// wait for callback handling
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!getNextFromQueueMessageHandled);
	}
}
