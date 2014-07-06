package org.hive2hive.core.processes.common.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.TestMessage;
import org.hive2hive.core.network.messages.TestMessageWithReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.common.base.BaseMessageProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link BaseMessageProcessStep} class. Checks if the process step successes when message
 * successfully arrives and if the process step fails (triggers rollback) when the sending of a message fails.
 * 
 * @author Seppi, Nico
 */
public class BaseMessageProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseMessageProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkTestUtil.setSameSession(network);
	}

	/**
	 * Sends an asynchronous message through a process step. This test checks if the process step successes
	 * when the message arrives at the right target node (node which is responsible for the given key). This
	 * is verified by locally storing and looking for the sent test data at the receiving node.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void baseMessageProcessStepTestOnSuccess() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(network.size() / 2));
		final NetworkManager nodeB = network.get(random.nextInt(network.size() / 2) + network.size() / 2);
		// generate random data and content key
		String data = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey);

		// check if selected location is empty
		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		// create a message with target node B
		final TestMessage message = new TestMessage(nodeB.getNodeId(), contentKey, new H2HTestData(data));

		// initialize the process and the one and only step to test
		BaseMessageProcessStep step = new BaseMessageProcessStep(nodeA.getMessageManager()) {

			@Override
			protected void doExecute() throws InvalidProcessStateException {
				try {
					send(message, getPublicKey(nodeB));
				} catch (SendFailedException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				Assert.fail("Should be not used.");
			}
		};
		TestExecutionUtil.executeProcess(step);

		// wait till message gets handled
		H2HWaiter w = new H2HWaiter(10);
		do {
			w.tickASecond();
			futureGet = nodeA.getDataManager().getUnblocked(parameters);
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// verify that data arrived
		String result = ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(data, result);
	}

	/**
	 * Sends an asynchronous message through a process step. This test checks if the process step fails
	 * when the message gets denied at the target node (node which is responsible for the given key).
	 * 
	 * @throws NoPeerConnectionException
	 * @throws InvalidProcessStateException
	 */
	@Test
	public void baseMessageProcessStepTestOnFailure() throws NoPeerConnectionException, InvalidProcessStateException {
		// select two random nodes
		NetworkManager nodeA = network.get(random.nextInt(network.size() / 2));
		final NetworkManager nodeB = network.remove(random.nextInt(network.size() / 2) + network.size() / 2);
		// generate random data and content key
		String data = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey);

		// check if selected location is empty
		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		// assign a denying message handler at target node
		nodeB.getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());

		// create a message with target node B
		final TestMessage message = new TestMessage(nodeB.getNodeId(), contentKey, new H2HTestData(data));

		// initialize the process and the one and only step to test
		BaseMessageProcessStep step = new BaseMessageProcessStep(nodeA.getMessageManager()) {

			@Override
			protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
				try {
					send(message, getPublicKey(nodeB));
					Assert.fail();
				} catch (SendFailedException e) {
					// expected
					throw new ProcessExecutionException("Expected behavior.", e);
				}
			}

			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				Assert.fail("Should be not used.");
			}
		};
		TestProcessComponentListener listener = new TestProcessComponentListener();
		step.attachListener(listener);
		step.start();
		// wait for the process to finish
		TestExecutionUtil.waitTillFailed(listener, 10);

		// check if selected location is still empty
		futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	/**
	 * Sends an asynchronous request message through a process step. This test checks if the process step
	 * successes when receiving node responds to a request message.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void baseMessageProcessStepTestWithARequestMessage() throws ClassNotFoundException, IOException,
			NoPeerConnectionException {
		// select two random nodes
		final NetworkManager nodeA = network.get(random.nextInt(network.size() / 2));
		final NetworkManager nodeB = network.get(random.nextInt(network.size() / 2) + network.size() / 2);
		// generate a random content key
		final String contentKey = NetworkTestUtil.randomString();
		final Parameters parametersA = new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(contentKey);
		final Parameters parametersB = new Parameters().setLocationKey(nodeB.getNodeId()).setContentKey(contentKey);

		// check if selected locations are empty
		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parametersB);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
		futureGet = nodeB.getDataManager().getUnblocked(parametersA);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		// create a message with target node B
		final TestMessageWithReply message = new TestMessageWithReply(nodeB.getNodeId(), contentKey);

		// initialize the process and the one and only step to test
		BaseMessageProcessStep step = new BaseMessageProcessStep(nodeA.getMessageManager()) {

			@Override
			protected void doExecute() throws InvalidProcessStateException {
				try {
					send(message, getPublicKey(nodeB));
				} catch (SendFailedException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public void handleResponseMessage(ResponseMessage responseMessage) {
				// locally store on requesting node received data
				String receivedSecret = (String) responseMessage.getContent();
				try {
					nodeA.getDataManager().putUnblocked(parametersA.setData(new H2HTestData(receivedSecret)))
							.awaitUninterruptibly();
				} catch (NoPeerConnectionException e) {
					Assert.fail(e.getMessage());
				}
			}
		};
		TestExecutionUtil.executeProcess(step);

		// wait till response message gets handled
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
			futureGet = nodeA.getDataManager().getUnblocked(parametersA);
			futureGet.awaitUninterruptibly();
		} while (futureGet.getData() == null);

		// load and verify if same secret was shared
		String receivedSecret = ((H2HTestData) futureGet.getData().object()).getTestString();
		futureGet = nodeA.getDataManager().getUnblocked(parametersB);
		futureGet.awaitUninterruptibly();
		String originalSecret = ((H2HTestData) futureGet.getData().object()).getTestString();

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

	private class DenyingMessageReplyHandler implements ObjectDataReply {
		@Override
		public Object reply(PeerAddress sender, Object request) throws Exception {
			return AcceptanceReply.FAILURE;
		}
	}
}
