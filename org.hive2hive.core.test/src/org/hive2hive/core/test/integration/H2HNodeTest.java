package org.hive2hive.core.test.integration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HNode;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.manager.ProcessManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests for the H2HNode.
 * 
 * @author Nico
 * 
 */
public class H2HNodeTest extends H2HJUnitTest {

	private static final int NETWORK_SIZE = 10;
	private static List<IH2HNode> network;
	private final Random random = new Random();

	private IH2HNode loggedInNode;

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

	@Before
	public void testRegisterLogin() throws IOException {
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		IH2HNode registerNode = network.get(random.nextInt(NETWORK_SIZE));
		IProcess registerProcess = registerNode.register(credentials);
		TestProcessListener listener = new TestProcessListener();
		registerProcess.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		loggedInNode = network.get(random.nextInt(NETWORK_SIZE));
		IProcess loginProcess = loggedInNode.login(credentials);
		listener = new TestProcessListener();
		loginProcess.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// wait for the post-loginprocess to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());
	}

	@Test
	public void testAddDeleteFile() throws IOException, IllegalFileLocation, NoSessionException {
		File rootDirectory = loggedInNode.getRootDirectory();
		File testFile = new File(rootDirectory, "test-file");
		FileUtils.write(testFile, "Hello World");

		IProcess process = loggedInNode.add(testFile);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(2000);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// is now added; delete it
		process = loggedInNode.delete(testFile);
		listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@Test
	public void testAddFileWrongDir() throws IOException, NoSessionException {
		File testFile = new File(FileUtils.getTempDirectory(), "test-file");
		FileUtils.write(testFile, "Hello World");

		try {
			loggedInNode.add(testFile);
			Assert.fail("Should not be able to add a file that is not in the root");
		} catch (IllegalFileLocation e) {
			// intended exception
		}
	}

	@After
	public void logoutAndUnregister() {
		// TODO logout and unregister

		// IProcess process = loggedInNode.logout();
		// TestProcessListener listener = new TestProcessListener();
		// process.addListener(listener);
		//
		// // wait for the process to finish
		// H2HWaiter waiter = new H2HWaiter(20);
		// do {
		// waiter.tickASecond();
		// } while (!listener.hasSucceeded());
	}
}
