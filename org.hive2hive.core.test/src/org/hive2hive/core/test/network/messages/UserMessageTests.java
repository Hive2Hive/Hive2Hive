package org.hive2hive.core.test.network.messages;

import java.util.List;
import java.util.Random;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.usermessages.ContactPeerUserMessage;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
				String responseEvidence = (String) responseMessage.getContent();
				Assert.assertTrue(evidence.equals(responseEvidence));
				logger.debug("ContactPeerUserMessage got handled.");
			}
		});

		// send message
		node1.sendDirect(message).addListener(new FutureDirectListener(new IBaseMessageListener() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				Assert.fail("The sending of the message failed.");
			}
		}, message, node1));
		
		// wait for callback handling
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!contactPeerUserMessageHandled);
	}

	public void GetNextFromQueueMessageTest() {

	}
}
