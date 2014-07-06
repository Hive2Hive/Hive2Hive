package org.hive2hive.core.processes.login;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
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

	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private File root0; // belongs to client 0
	private File root1; // belongs to client 1

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SynchronizeFilesStepTest.class;
		beforeClass();
	}

	@Before
	public void setupFiles() throws IOException, IllegalFileLocation, NoSessionException, NoPeerConnectionException {
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkManager uploader = network.get(0);

		// create two root directories
		root0 = FileTestUtil.getTempDirectory();
		root1 = FileTestUtil.getTempDirectory();

		// first, register and login a new user
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userCredentials, uploader, root0);

		// create default tree that can be used later. Upload it to the DHT
		// - file 1
		// - file 2
		// - folder 1
		// - - file 3
		File file1 = new File(root0, "file 1");
		FileUtils.writeStringToFile(file1, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(uploader, file1);

		File file2 = new File(root0, "file 2");
		FileUtils.writeStringToFile(file2, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(uploader, file2);

		File folder1 = new File(root0, "folder 1");
		folder1.mkdir();
		UseCaseTestUtil.uploadNewFile(uploader, folder1);

		File file3 = new File(folder1, "file 3");
		FileUtils.writeStringToFile(file3, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(uploader, file3);

		// copy the content to the other client such that they are in sync
		FileUtils.copyDirectory(root0, root1);

		// write both versions to disc
		FileUtil.writePersistentMetaData(uploader.getSession().getRoot(), null, null);
		FileUtil.writePersistentMetaData(root1.toPath(), null, null);
	}

	@Test
	public void testNothingChanged() throws NoSessionException, InvalidProcessStateException, NoPeerConnectionException {
		UseCaseTestUtil.login(userCredentials, network.get(1), root1);
		UseCaseTestUtil.synchronize(network.get(1));

		// check if the size is still the same
		Assert.assertEquals(FileUtils.sizeOfAsBigInteger(root0), FileUtils.sizeOfAsBigInteger(root1));
	}

	@Test
	public void testAdditionsDeletions() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			InvalidProcessStateException, NoPeerConnectionException {
		/** do some modifications on client **/
		// add a file
		FileUtils.write(new File(root1, "added-file"), NetworkTestUtil.randomString());

		// delete file 1
		File file1 = new File(root1, "file 1");
		file1.delete();

		/** do some modifications on the remote **/
		NetworkManager remoteClient = network.get(0);

		// add a file 4 within folder 1
		File file4 = new File(new File(root0, "folder 1"), "file 4");
		FileUtils.write(file4, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(remoteClient, file4);

		// delete file 2
		File file2 = new File(root0, "file 2");
		file2.delete();
		UseCaseTestUtil.deleteFile(remoteClient, file2);

		/** start sync **/
		UseCaseTestUtil.login(userCredentials, network.get(1), root1);
		UseCaseTestUtil.synchronize(network.get(1));

		/** verify if the remote changes are applied **/
		file4 = new File(new File(root1, "folder 1"), "file 4");
		Assert.assertTrue(file4.exists()); // added file is now here
		file2 = new File(root1, "file 2");
		Assert.assertFalse(file2.exists()); // deleted file is not here

		/** verify if the local changes have been uploaded **/
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(network.get(0), userCredentials);
		// added file is here
		Assert.assertTrue(userProfile.getFileByPath(Paths.get("added-file")) != null);
		// deleted file is not in user profile
		Assert.assertTrue(userProfile.getFileByPath(Paths.get("file 1")) == null);
	}

	@Test
	public void testModifications() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			InvalidProcessStateException, IllegalArgumentException, NoPeerConnectionException {
		/** do some modifications on client **/
		// modify file 1
		File file1 = new File(root1, "file 1");
		FileUtils.write(file1, NetworkTestUtil.randomString());
		byte[] newMD5File1 = HashUtil.hash(file1);

		// modify file 3
		File folder = new File(root1, "folder 1");
		File file3 = new File(folder, "file 3");
		FileUtils.write(file3, NetworkTestUtil.randomString());

		/** do some modifications on the remote **/
		NetworkManager remoteClient = network.get(0);

		// modify file 2
		File file2 = new File(root0, "file 2");
		String file2Content = NetworkTestUtil.randomString();
		FileUtils.write(file2, file2Content);
		UseCaseTestUtil.uploadNewVersion(remoteClient, file2);

		// also modify file 3
		folder = new File(root0, "folder 1");
		file3 = new File(folder, "file 3");
		FileUtils.write(file3, NetworkTestUtil.randomString());
		byte[] newMD5File3 = HashUtil.hash(file3);
		UseCaseTestUtil.uploadNewVersion(remoteClient, file3);

		/** start sync **/
		UseCaseTestUtil.login(userCredentials, network.get(1), root1);
		UseCaseTestUtil.synchronize(network.get(1));

		/** verify if the remote changes are applied **/
		// modification of file 2 has been downloaded
		file2 = new File(root1, "file 2");
		Assert.assertEquals(file2Content, FileUtils.readFileToString(file2));

		/** verify if the local changes have been uploaded **/
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(network.get(1), userCredentials);
		FileIndex file1Node = (FileIndex) userProfile.getFileByPath(Paths.get("file 1"));
		// modifications have been uploaded
		Assert.assertTrue(HashUtil.compare(newMD5File1, file1Node.getMD5()));

		/** verify the file that has been modified remotely and locally **/
		FileIndex file3Node = (FileIndex) userProfile.getFileByPath(Paths.get("folder 1", "file 3"));
		Assert.assertTrue(HashUtil.compare(newMD5File3, file3Node.getMD5()));
	}

	@After
	public void tearDown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(root0);
		FileUtils.deleteDirectory(root1);
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
}
