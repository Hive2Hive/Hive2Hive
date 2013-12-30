package org.hive2hive.core.test.process.login.postLogin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.process.login.SynchronizeFilesStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
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
	private static IFileConfiguration config = new TestFileConfiguration();

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
		ProcessTestUtil.register(userCredentials, network.get(0));
		UserProfileManager profileManager = new UserProfileManager(network.get(0), userCredentials);

		// create two filemanagers
		File root1 = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager0 = new FileManager(root1.toPath());
		File root2 = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager1 = new FileManager(root2.toPath());

		// create default tree that can be used later. Upload it to the DHT
		// - file 1
		// - file 2
		// - folder 1
		// - - file 3
		File file1 = new File(fileManager0.getRoot().toFile(), "file 1");
		FileUtils.writeStringToFile(file1, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file1, profileManager, fileManager0, config);

		File file2 = new File(fileManager0.getRoot().toFile(), "file 2");
		FileUtils.writeStringToFile(file2, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file2, profileManager, fileManager0, config);

		File folder1 = new File(fileManager0.getRoot().toFile(), "folder 1");
		folder1.mkdir();
		ProcessTestUtil.uploadNewFile(network.get(0), folder1, profileManager, fileManager0, config);

		File file3 = new File(folder1, "file 3");
		FileUtils.writeStringToFile(file3, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(network.get(0), file3, profileManager, fileManager0, config);

		// copy the content to the other client such that they are in sync
		FileUtils.copyDirectory(fileManager0.getRoot().toFile(), fileManager1.getRoot().toFile());

		// write both versions to disc
		fileManager0.writePersistentMetaData();
		fileManager1.writePersistentMetaData();
	}

	@Test
	public void testNothingChanged() throws NoSessionException {
		// the client that logs in
		NetworkManager client = network.get(1);

		startSync(client, fileManager1, 20);

		// check if the size is still the same
		Assert.assertEquals(FileUtils.sizeOfAsBigInteger(fileManager0.getRoot().toFile()),
				FileUtils.sizeOfAsBigInteger(fileManager1.getRoot().toFile()));
	}

	@Test
	public void testAdditionsDeletions() throws IOException, IllegalFileLocation, NoSessionException {
		/** do some modifications on client **/
		// add a file
		FileUtils.write(new File(fileManager1.getRoot().toFile(), "added-file"),
				NetworkTestUtil.randomString());

		// delete file 1
		File file1 = new File(fileManager1.getRoot().toFile(), "file 1");
		file1.delete();

		/** do some modifications on the remote **/
		NetworkManager remoteClient = network.get(0);
		UserProfileManager profileManager = new UserProfileManager(remoteClient, userCredentials);

		// add a file 4 within folder 1
		File file4 = new File(new File(fileManager0.getRoot().toFile(), "folder 1"), "file 4");
		FileUtils.write(file4, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(remoteClient, file4, profileManager, fileManager0, config);

		// delete file 2
		File file2 = new File(fileManager0.getRoot().toFile(), "file 2");
		file2.delete();
		ProcessTestUtil.deleteFile(remoteClient, file2, profileManager, fileManager0, config);

		/** start sync **/
		// the client that logs in
		NetworkManager client = network.get(1);
		startSync(client, fileManager1, 60);

		/** verify if the remote changes are applied **/
		file4 = new File(new File(fileManager1.getRoot().toFile(), "folder 1"), "file 4");
		Assert.assertTrue(file4.exists()); // added file is now here
		file2 = new File(fileManager1.getRoot().toFile(), "file 2");
		Assert.assertFalse(file2.exists()); // deleted file is not here

		/** verify if the local changes have been uploaded **/
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertTrue(userProfile.getFileByPath(Paths.get("added-file")) != null); // added file is here
		Assert.assertTrue(userProfile.getFileByPath(Paths.get("file 1")) == null); // deleted file is not in
																					// UP
	}

	@Test
	public void testModifications() throws IOException, IllegalFileLocation, NoSessionException {
		/** do some modifications on client **/
		// modify file 1
		File file1 = new File(fileManager1.getRoot().toFile(), "file 1");
		FileUtils.write(file1, NetworkTestUtil.randomString());
		byte[] newMD5File1 = EncryptionUtil.generateMD5Hash(file1);

		// modify file 3
		File folder = new File(fileManager1.getRoot().toFile(), "folder 1");
		File file3 = new File(folder, "file 3");
		FileUtils.write(file3, NetworkTestUtil.randomString());

		/** do some modifications on the remote **/
		NetworkManager remoteClient = network.get(0);
		UserProfileManager profileManager = new UserProfileManager(remoteClient, userCredentials);

		// modify file 2
		File file2 = new File(fileManager0.getRoot().toFile(), "file 2");
		String file2Content = NetworkTestUtil.randomString();
		FileUtils.write(file2, file2Content);
		ProcessTestUtil.uploadNewFileVersion(remoteClient, file2, profileManager, fileManager0, config);

		// also modify file 3
		folder = new File(fileManager0.getRoot().toFile(), "folder 1");
		file3 = new File(folder, "file 3");
		FileUtils.write(file3, NetworkTestUtil.randomString());
		byte[] newMD5File3 = EncryptionUtil.generateMD5Hash(file3);
		ProcessTestUtil.uploadNewFileVersion(remoteClient, file3, profileManager, fileManager0, config);

		/** start sync **/
		// the client that logs in
		NetworkManager client = network.get(1);
		startSync(client, fileManager1, 60);

		/** verify if the remote changes are applied **/
		// modification of file 2 has been downloaded
		file2 = new File(fileManager1.getRoot().toFile(), "file 2");
		Assert.assertEquals(file2Content, FileUtils.readFileToString(file2));

		/** verify if the local changes have been uploaded **/
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		FileTreeNode file1Node = userProfile.getFileByPath(Paths.get("file 1"));
		// modifications have been uploaded
		Assert.assertTrue(H2HEncryptionUtil.compareMD5(newMD5File1, file1Node.getMD5()));

		/** verify the file that has been modified remotely and locally **/
		FileTreeNode file3Node = userProfile.getFileByPath(Paths.get("folder 1", "file 3"));
		Assert.assertTrue(H2HEncryptionUtil.compareMD5(newMD5File3, file3Node.getMD5()));
	}

	private void startSync(NetworkManager client, FileManager fileManager, int waitTimeS)
			throws NoSessionException {
		UserProfileManager manager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				manager, config, fileManager));
		SynchronizePostLoginProcess process = new SynchronizePostLoginProcess(client, manager, fileManager);
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
		FileUtils.deleteDirectory(fileManager0.getRoot().toFile());
		FileUtils.deleteDirectory(fileManager1.getRoot().toFile());
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

		public SynchronizePostLoginProcess(NetworkManager networkManager, UserProfileManager profileManager,
				FileManager fileManager) throws NoSessionException {
			super(null, networkManager);
			super.getContext().setIsElectedMaster(false);
			setNextStep(new SynchronizeFilesStep());
		}

	}
}
