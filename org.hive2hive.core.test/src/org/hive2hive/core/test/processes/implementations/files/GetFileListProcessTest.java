package org.hive2hive.core.test.processes.implementations.files;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.TestResultProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetFileListProcessTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static UserCredentials credentials;
	private static File root;
	private static FileManager fileManager;
	private static IFileConfiguration fileConfig;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetFileListProcessTest.class;
		beforeClass();
	}

	@Before
	public void setup() throws NoPeerConnectionException {
		super.beforeMethod();

		// network
		network = NetworkTestUtil.createNetwork(2);
		credentials = NetworkTestUtil.generateRandomCredentials();

		UseCaseTestUtil.register(credentials, network.get(0));

		// files
		root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager = new FileManager(root.toPath());
		fileConfig = new TestFileConfiguration();
	}

	@After
	public void tearDown() throws IOException {
		super.afterMethod();

		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManager.getRoot().toFile());
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	@Test
	public void getFileListTest() throws IOException, IllegalFileLocation, InvalidProcessStateException {

		NetworkManager client = NetworkTestUtil.getRandomNode(network);
		UserProfileManager profileManager = new UserProfileManager(client, credentials);

		// set session
		SessionParameters params = new SessionParameters();
		params.setKeyPair(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS));
		params.setProfileManager(profileManager);
		params.setFileConfig(fileConfig);
		params.setFileManager(fileManager);

		H2HSession session = new H2HSession(params);
		client.setSession(session);

		// TODO maybe move to UseCaseTestUtil
		// test process
		IResultProcessComponent<List<Path>> fileListProcess = ProcessFactory.instance()
				.createFileListProcess(client);
		TestResultProcessComponentListener<List<Path>> listener = new TestResultProcessComponentListener<List<Path>>();
		fileListProcess.attachListener(listener);
		fileListProcess.start();

		H2HWaiter waiter = new H2HWaiter(1000);
		do {
			waiter.tickASecond();
		} while (!listener.hasResultArrived());

		assertEquals(1, listener.getResult().size());

		// TODO get sure about what a digest should actually deliver and adapt asserts below
		// // add child1 to the network
		// File child1 = new File(root, NetworkTestUtil.randomString());
		// FileUtils.writeStringToFile(child1, NetworkTestUtil.randomString());
		// ProcessTestUtil.uploadNewFile(client, child1, profileManager, fileManager, fileConfig);
		//
		// fileListProcess = ProcessFactory.instance().createFileListProcess(client);
		// listener = new TestResultProcessComponentListener<List<Path>>();
		// fileListProcess.attachListener(listener);
		// fileListProcess.start();
		//
		// waiter = new H2HWaiter(10);
		// do {
		// if (listener.hasFailed())
		// Assert.fail();
		// waiter.tickASecond();
		// } while (!listener.hasResultArrived());
		//
		// assertEquals(1, listener.getResult().size());
		// assertEquals(root.toPath().relativize(child1.toPath()).toString(), listener.getResult().get(0)
		// .toString());
		//
		// // add dir1 to the network
		// File dir1 = new File(root, NetworkTestUtil.randomString());
		// dir1.mkdir();
		// ProcessTestUtil.uploadNewFile(client, dir1, profileManager, fileManager, fileConfig);
		//
		// // add dir1/child1 to the network
		// File dir1Child1 = new File(dir1, NetworkTestUtil.randomString());
		// FileUtils.writeStringToFile(dir1Child1, NetworkTestUtil.randomString());
		// ProcessTestUtil.uploadNewFile(client, dir1Child1, profileManager, fileManager, fileConfig);
		//
		// fileListProcess = ProcessFactory.instance().createFileListProcess(client);
		// listener = new TestResultProcessComponentListener<List<Path>>();
		// fileListProcess.attachListener(listener);
		// fileListProcess.start();
		//
		// waiter = new H2HWaiter(10);
		// do {
		// if (listener.hasFailed())
		// Assert.fail();
		// waiter.tickASecond();
		// } while (!listener.hasResultArrived());
		//
		// assertEquals(2, listener.getResult());
		// assertEquals(root.toPath().relativize(dir1Child1.toPath()).toString(), listener.getResult().get(1)
		// .toString());
		//
		// // delete child1 from the network
		// ProcessTestUtil.deleteFile(client, child1, profileManager, fileManager, fileConfig);
		//
		// fileListProcess = ProcessFactory.instance().createFileListProcess(client);
		// listener = new TestResultProcessComponentListener<List<Path>>();
		// fileListProcess.attachListener(listener);
		// fileListProcess.start();
		//
		// waiter = new H2HWaiter(10);
		// do {
		// if (listener.hasFailed())
		// Assert.fail();
		// waiter.tickASecond();
		// } while (!listener.hasResultArrived());
		//
		// assertEquals(1, listener.getResult().size());
		// assertEquals(root.toPath().relativize(dir1Child1.toPath()).toString(), listener.getResult().get(0)
		// .toString());
	}
}
