package org.hive2hive.core.test.integration;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.IH2HNode;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class H2HNodeTest extends H2HJUnitTest {

	private static final int NETWORK_SIZE = 10;
	private static List<IH2HNode> network;
	private final Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HNodeTest.class;
		beforeClass();
		network = NetworkTestUtil.createH2HNetwork(NETWORK_SIZE);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownH2HNetwork(network);
		afterClass();
	}

	@Test
	public void testRegisterLogin() throws InterruptedException, IOException, ClassNotFoundException {
		String userName = NetworkTestUtil.randomString();
		String password = NetworkTestUtil.randomString();
		String pin = NetworkTestUtil.randomString();

		IH2HNode registerNode = network.get(random.nextInt(NETWORK_SIZE));
		IProcess registerProcess = registerNode.register(userName, password, pin);
		TestProcessListener listener = new TestProcessListener();
		registerProcess.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		IH2HNode loginNode = network.get(random.nextInt(NETWORK_SIZE));
		IProcess loginProcess = loginNode.login(userName, password, pin);
		listener = new TestProcessListener();
		loginProcess.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}
}
