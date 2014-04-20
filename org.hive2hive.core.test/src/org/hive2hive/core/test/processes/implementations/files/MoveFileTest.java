package org.hive2hive.core.test.processes.implementations.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests moving a file.
 * 
 * @author Nico
 * 
 */
public class MoveFileTest extends H2HJUnitTest {

	private static final int networkSize = 5;
	private static List<NetworkManager> network;
	private final static int CHUNK_SIZE = 1024;
	private UserCredentials userCredentials;
	private File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MoveFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void register() throws NoPeerConnectionException {
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		UseCaseTestUtil.register(userCredentials, network.get(0));
		root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
	}

	@Test
	public void testRootToDirectory() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(1);
		UseCaseTestUtil.login(userCredentials, client, root);

		// add a file to the network
		File file = FileTestUtil.createFileRandomContent(3, root, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(client, file);

		// add the target directory
		File folder = new File(root, "folder");
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
	public void testDirectoryToRoot() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(1);
		UseCaseTestUtil.login(userCredentials, client, root);

		// add the source folder
		File folder = new File(root, "folder");
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		// add a file to the folder
		File file = new File(folder, "test-file");
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

		// root contains moved file and empty folder as file
		Assert.assertEquals(2, userProfile.getRoot().getChildren().size());
	}

	@Test
	public void testDirectoryToDirectory() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(1);
		UseCaseTestUtil.login(userCredentials, client, root);

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
	public void testRename() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		NetworkManager client = network.get(1);
		UseCaseTestUtil.login(userCredentials, client, root);

		// add a file to the network
		File file = new File(root, "test-file");
		FileUtils.write(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		// don't move, only rename
		File destination = new File(root, "test-file-moved");

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
		afterClass();
	}
}
