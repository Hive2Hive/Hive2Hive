package org.hive2hive.core.processes.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests moving a file.
 * 
 * @author Nico, Seppi
 */
public class MoveFileTest extends H2HJUnitTest {

	private final static int networkSize = 2;

	private static List<NetworkManager> network;
	private static NetworkManager client;
	private static UserCredentials userCredentials;
	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MoveFileTest.class;
		beforeClass();

		// setup a network
		network = NetworkTestUtil.createNetwork(networkSize);
		client = network.get(1);

		// create a user
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		// register user
		UseCaseTestUtil.register(userCredentials, client);
		root = FileTestUtil.getTempDirectory();
		// login user
		UseCaseTestUtil.login(userCredentials, client, root);
	}

	@Test
	public void testRootToDirectory() throws IOException, IllegalFileLocation, GetFailedException, InterruptedException,
			NoSessionException, NoPeerConnectionException {
		// add a file to the root
		File file = new File(root, "test-file-from-root-to-folder");
		FileUtils.write(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		// add the target directory
		File folder = new File(root, "from-root-to-target-folder");
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		File destination = new File(folder, file.getName());

		// move the file
		UseCaseTestUtil.moveFile(client, file, destination);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Index fileNode = userProfile.getFileByPath(destination, root.toPath());
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(folder.getName(), fileNode.getParent().getName());
	}

	@Test
	public void testDirectoryToRoot() throws IOException, IllegalFileLocation, GetFailedException, InterruptedException,
			NoSessionException, NoPeerConnectionException {
		// add the source folder
		File folder = new File(root, "from-source-folder-to-root");
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		// add a file to the folder
		File file = new File(folder, "test-file-from-folder-to-root");
		FileUtils.write(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		File destination = new File(root, file.getName());

		// move the file
		UseCaseTestUtil.moveFile(client, file, destination);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Index fileNode = userProfile.getFileByPath(destination, root.toPath());
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(userProfile.getRoot(), fileNode.getParent());
	}

	@Test
	public void testDirectoryToDirectory() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		// add the source folder
		File sourceFolder = new File(root, "source-folder");
		sourceFolder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, sourceFolder);

		// add a file to the folder
		File file = new File(sourceFolder, "test-file");
		FileUtils.write(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		// add the destination folder
		File destFolder = new File(root, "dest-folder");
		destFolder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, destFolder);

		File destination = new File(destFolder, file.getName());

		// move the file
		UseCaseTestUtil.moveFile(client, file, destination);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Index fileNode = userProfile.getFileByPath(destination, root.toPath());
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(destFolder.getName(), fileNode.getParent().getName());
	}

	@Test
	public void testRename() throws IOException, IllegalFileLocation, GetFailedException, InterruptedException,
			NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(1);
		UseCaseTestUtil.login(userCredentials, client, root);

		// add a file to the network
		File file = new File(root, "test-file-to-rename");
		FileUtils.write(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		// don't move, only rename
		File destination = new File(root, "test-file-renamed");

		// move the file
		UseCaseTestUtil.moveFile(client, file, destination);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Index fileNode = userProfile.getFileByPath(destination, root.toPath());
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(fileNode.getName(), destination.getName());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(root);
		afterClass();
	}
}
