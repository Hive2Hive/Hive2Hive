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
import org.hive2hive.core.process.ProcessManager;
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

	private static final int NETWORK_SIZE = 5;
	private static List<IH2HNode> network;
	private final Random random = new Random();

	private IH2HNode loggedInNode;
	private File rootDirectory;
	private UserCredentials credentials;

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
		credentials = NetworkTestUtil.generateRandomCredentials();

		IH2HNode registerNode = network.get(random.nextInt(NETWORK_SIZE));
		IProcess registerProcess = registerNode.getUserManagement().register(credentials);
		TestProcessListener listener = new TestProcessListener();
		registerProcess.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		rootDirectory = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		loggedInNode = network.get(random.nextInt(NETWORK_SIZE / 2));
		IProcess loginProcess = loggedInNode.getUserManagement().login(credentials, rootDirectory.toPath());
		listener = new TestProcessListener();
		loginProcess.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@Test
	public void testAddDeleteFile() throws IOException, IllegalFileLocation, NoSessionException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		File testFile = new File(rootDirectory, "test-file");
		FileUtils.write(testFile, "Hello World");

		IProcess process = loggedInNode.getFileManagement().add(testFile);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// is now added; delete it
		process = loggedInNode.getFileManagement().delete(testFile);
		listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@Test
	public void testAddFileWrongDir() throws IOException, NoSessionException {
		File testFile = new File(FileUtils.getTempDirectory(), "test-file");
		FileUtils.write(testFile, "Hello World");

		try {
			loggedInNode.getFileManagement().add(testFile);
			Assert.fail("Should not be able to add a file that is not in the root");
		} catch (IllegalFileLocation e) {
			// intended exception
		}
	}

	@Test
	public void testAddFileTree() throws IOException, IllegalFileLocation, NoSessionException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// /folder1/test1.txt
		// /folder1/folder2/test2.txt
		File folder1 = new File(rootDirectory, "folder1");
		folder1.mkdir();
		File test1File = new File(folder1, "test1.txt");
		FileUtils.write(test1File, "Hello World 1");

		File folder2 = new File(folder1, "folder2");
		folder2.mkdir();
		File test2File = new File(folder2, "test2.txt");
		FileUtils.write(test2File, "Hello World 2");

		IProcess process = loggedInNode.getFileManagement().add(folder1);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish the upload
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// wait for all uploading processes to finish
		waiter = new H2HWaiter(60);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// then start 2nd client and login
		File rootUser2 = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		IH2HNode newNode = network.get((random.nextInt(NETWORK_SIZE / 2) + NETWORK_SIZE / 2));
		IProcess loginProcess = newNode.getUserManagement().login(credentials, rootUser2.toPath());
		listener = new TestProcessListener();
		loginProcess.addListener(listener);

		// wait for the process to finish
		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// wait for the post-loginprocess to finish
		waiter = new H2HWaiter(60);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// verfiy that all files are here
		folder1 = new File(rootUser2, "folder1");
		Assert.assertTrue(folder1.exists());

		test1File = new File(folder1, "test1.txt");
		Assert.assertEquals("Hello World 1", FileUtils.readFileToString(test1File));

		folder2 = new File(folder1, "folder2");
		Assert.assertTrue(folder2.exists());

		test2File = new File(folder2, "test2.txt");
		Assert.assertEquals("Hello World 2", FileUtils.readFileToString(test2File));
	}

	@After
	public void logoutAndUnregister() throws NoSessionException {
		IProcess process = loggedInNode.getUserManagement().logout();
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// TODO unregister
	}
}
