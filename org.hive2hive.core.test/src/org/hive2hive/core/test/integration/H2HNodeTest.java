package org.hive2hive.core.test.integration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.H2HFileManager;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.H2HUserManager;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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
	private static List<H2HNode> network;
	private final Random random = new Random();

	private H2HNode loggedInNode;
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
	public void testRegisterLogin() throws IOException, NoPeerConnectionException, NoNetworkException {
		credentials = NetworkTestUtil.generateRandomCredentials();

		H2HNode registerNode = network.get(random.nextInt(NETWORK_SIZE));
		H2HUserManager userManager = new H2HUserManager();
		registerNode.attach(userManager);
		IProcessComponent registerProcess = userManager.register(credentials);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		registerProcess.attachListener(listener);
		UseCaseTestUtil.waitTillSucceded(listener, 20);

		rootDirectory = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		loggedInNode = network.get(random.nextInt(NETWORK_SIZE / 2));
		IProcessComponent loginProcess = userManager.login(credentials, FileConfiguration.createDefault(),
				rootDirectory.toPath());
		TestProcessComponentListener loginListener = new TestProcessComponentListener();
		loginProcess.attachListener(loginListener);
		UseCaseTestUtil.waitTillSucceded(loginListener, 20);
	}

	@Test
	public void testAddDeleteFile() throws IOException, IllegalFileLocation, NoSessionException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			NoPeerConnectionException, NoNetworkException {
		File testFile = new File(rootDirectory, "test-file");
		FileUtils.write(testFile, "Hello World");

		H2HFileManager fileManager = new H2HFileManager(FileConfiguration.createDefault());
		loggedInNode.attach(fileManager);
		IProcessComponent process = fileManager.add(testFile);
		UseCaseTestUtil.executeProcess(process);

		// is now added; delete it
		process = fileManager.delete(testFile);
		UseCaseTestUtil.executeProcess(process);
	}

	@Test(expected = IllegalFileLocation.class)
	public void testAddFileWrongDir() throws IOException, NoSessionException, IllegalFileLocation,
			NoPeerConnectionException, NoNetworkException {
		File testFile = new File(FileUtils.getTempDirectory(), "test-file");
		FileUtils.write(testFile, "Hello World");

		H2HFileManager fileManager = new H2HFileManager(FileConfiguration.createDefault());
		loggedInNode.attach(fileManager);
		fileManager.add(testFile);
	}

	@Test
	public void testAddFileTree() throws IOException, IllegalFileLocation, NoSessionException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			NoPeerConnectionException, NoNetworkException {
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

		H2HFileManager fileManager = new H2HFileManager(FileConfiguration.createDefault());
		loggedInNode.attach(fileManager);
		IProcessComponent process = fileManager.add(folder1);
		UseCaseTestUtil.executeProcess(process);

		// TODO wait for all async process to upload

		// then start 2nd client and login
		File rootUser2 = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		H2HNode newNode = network.get((random.nextInt(NETWORK_SIZE / 2) + NETWORK_SIZE / 2));
		
		H2HUserManager userManager = new H2HUserManager();
		newNode.attach(userManager);
		IProcessComponent loginProcess = userManager.login(credentials, FileConfiguration.createDefault(), rootUser2.toPath());
		UseCaseTestUtil.executeProcess(loginProcess);

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
	public void logoutAndUnregister() throws NoSessionException, NoPeerConnectionException, NoNetworkException {
		H2HUserManager userManager = new H2HUserManager();
		loggedInNode.attach(userManager);
		IProcessComponent process = userManager.logout();
		UseCaseTestUtil.executeProcess(process);

		// TODO unregister
	}
}
