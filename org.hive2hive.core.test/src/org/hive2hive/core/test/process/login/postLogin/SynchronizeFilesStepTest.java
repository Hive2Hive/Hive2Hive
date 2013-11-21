package org.hive2hive.core.test.process.login.postLogin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.login.SynchronizeFilesStep;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Creates two file trees and compares them with each other. The test verifies whether all changes during
 * absence are done correctly.
 * 
 * @author Nico
 * 
 */
public class SynchronizeFilesStepTest extends H2HJUnitTest {

	private static final int networkSize = 2;
	private static IH2HFileConfiguration config = new TestH2HFileConfiguration();

	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private FileManager fileManager0; // belongs to client 0
	private FileManager fileManager1; // belongs to client 1

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SynchronizeFilesStepTest.class;
		beforeClass();
	}

	@Before
	public void setupFiles() throws IOException, IllegalFileLocation {
		network = NetworkTestUtil.createNetwork(networkSize);

		// first, register a new user
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		ProcessTestUtil.register(network.get(0), userCredentials);

		// create two filemanagers
		File root1 = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager0 = new FileManager(root1);
		File root2 = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager1 = new FileManager(root2);

		// create default tree that can be used later. Upload it to the DHT
		// - file 1
		// - file 2
		// - folder 1
		// - - file 3
		File file1 = new File(fileManager0.getRoot(), "file 1");
		FileUtils.writeStringToFile(file1, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file1, userCredentials, fileManager0, config);

		File file2 = new File(fileManager0.getRoot(), "file 2");
		FileUtils.writeStringToFile(file2, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file2, userCredentials, fileManager0, config);

		File folder1 = new File(fileManager0.getRoot(), "folder 1");
		folder1.mkdir();
		ProcessTestUtil.uploadNewFile(network.get(0), folder1, userCredentials, fileManager0, config);

		File file3 = new File(folder1, "file 2");
		FileUtils.writeStringToFile(file3, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file3, userCredentials, fileManager0, config);

		// copy the content to the other client such that they are in sync
		FileUtils.copyDirectory(fileManager0.getRoot(), fileManager1.getRoot());

		// write both versions to disc
		fileManager0.writePersistentMetaData();
		fileManager1.writePersistentMetaData();
	}

	@Test
	public void testNothingChanged() {
		// the client that logs in
		NetworkManager client = network.get(1);

		startSync(client, fileManager1, 20);

		// check if the size is still the same
		Assert.assertEquals(FileUtils.sizeOfAsBigInteger(fileManager0.getRoot()),
				FileUtils.sizeOfAsBigInteger(fileManager1.getRoot()));
	}

	@Test
	public void testAdditionsDeletions() throws IOException, IllegalFileLocation {
		/** do some modifications on client **/
		// add a file
		FileUtils.write(new File(fileManager1.getRoot(), "added-file"), NetworkTestUtil.randomString());

		// delete file 1
		File file1 = new File(fileManager1.getRoot(), "file 1");
		file1.delete();

		/** do some modifications on the remote **/
		// add a file 3 within folder 1
		File file3 = new File(new File(fileManager0.getRoot(), "folder 1"), "file 4");
		FileUtils.write(file3, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file3, userCredentials, fileManager0, config);

		// delete file 2
		File file2 = new File(fileManager0.getRoot(), "file 2");
		file2.delete();
		ProcessTestUtil.deleteFile(network.get(0), file2, userCredentials, fileManager0);

		/** start sync **/
		// the client that logs in
		NetworkManager client = network.get(1);
		startSync(client, fileManager1, 60);

		/** verify if the remote changes are applied **/
		file3 = new File(new File(fileManager1.getRoot(), "folder 1"), "file 4");
		Assert.assertTrue(file3.exists()); // added file is now here
		file2 = new File(fileManager1.getRoot(), "file 2");
		Assert.assertFalse(file2.exists()); // deleted file is not here

		/** verify if the local changes have been uploaded **/
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertTrue(userProfile.getFileByPath("added-file") != null); // added file is here
		Assert.assertTrue(userProfile.getFileByPath("file 1") == null); // deleted file is not in UP
	}

	private void startSync(NetworkManager client, FileManager fileManager, int waitTimeS) {
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		SynchronizePostLoginProcess process = new SynchronizePostLoginProcess(client, userProfile,
				fileManager);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(waitTimeS);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@After
	public void tearDown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManager0.getRoot());
		FileUtils.deleteDirectory(fileManager1.getRoot());
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	/**
	 * Helper class to just start the synchronize step
	 * 
	 * @author Nico
	 * 
	 */
	private class SynchronizePostLoginProcess extends PostLoginProcess {

		public SynchronizePostLoginProcess(NetworkManager networkManager, UserProfile userProfile,
				FileManager fileManager) {
			super(userProfile, userCredentials, null, networkManager, fileManager, config);
			super.getContext().setIsElectedMaster(false);
			setNextStep(new SynchronizeFilesStep());
		}

	}
}
