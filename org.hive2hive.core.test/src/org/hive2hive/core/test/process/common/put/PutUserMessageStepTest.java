package org.hive2hive.core.test.process.common.put;

import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.usermessages.direct.ContactPeerUserMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.put.PutProcessStep;
import org.hive2hive.core.process.common.put.PutUserMessageStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PutUserMessageStepTest extends H2HJUnitTest {

	private Random random = new Random();
	private List<NetworkManager> network;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutUserMessageStepTest.class;
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

	@Test
	public void testStepSuccessful() {

		// select two random nodes
		NetworkManager node1 = network.get(random.nextInt(network.size()));
		NetworkManager node2 = network.get(random.nextInt(network.size()));
		
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		KeyPair keyPair = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		String evidence = NetworkTestUtil.randomString();
		
		// create user message
		ContactPeerUserMessage userMessage = new ContactPeerUserMessage(node2.getPeerAddress(), evidence);
		
		// create step
		PutUserMessageStep step = new PutUserMessageStep(credentials.getUserId(), userMessage, null, keyPair.getPrivate());
		
		// start process
		Process process = new Process(node1) {};
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

}
