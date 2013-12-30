package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
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
	private static IFileConfiguration config = new TestFileConfiguration();
	private UserCredentials userCredentials;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MoveFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void register() {
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(userCredentials, network.get(0));
	}

	@Test
	public void testRootToDirectory() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);

		// add a file to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root.toPath());
		File file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// add the target directory
		File folder = new File(fileManager.getRoot().toFile(), "folder");
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, profileManager, fileManager, config);

		File destination = new File(folder, file.getName());

		// move the file
		ProcessTestUtil.moveFile(client, file, destination, profileManager, fileManager, config);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(destination, fileManager);
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(folder.getName(), fileNode.getParent().getName());

		MetaDocument parentMetaDocument = ProcessTestUtil.getMetaDocument(client, fileNode.getParent()
				.getKeyPair());
		MetaFolder parentFolder = (MetaFolder) parentMetaDocument;
		Assert.assertEquals(1, parentFolder.getChildKeys().size());
	}

	@Test
	public void testDirectoryToRoot() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManager = new FileManager(root.toPath());

		// add the source folder
		File folder = new File(root, "folder");
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, profileManager, fileManager, config);

		// add a file to the folder
		File file = new File(folder, "test-file");
		FileUtils.write(file, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		File destination = new File(root, file.getName());

		// move the file
		ProcessTestUtil.moveFile(client, file, destination, profileManager, fileManager, config);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(destination, fileManager);
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(userProfile.getRoot(), fileNode.getParent());

		// root contains moved file and empty folder as file
		Assert.assertEquals(2, userProfile.getRoot().getChildren().size());
	}

	@Test
	public void testDirectoryToDirectory() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManager = new FileManager(root.toPath());

		// add the source folder
		File sourceFolder = new File(root, "source-folder");
		sourceFolder.mkdir();
		ProcessTestUtil.uploadNewFile(client, sourceFolder, profileManager, fileManager, config);

		// add a file to the folder
		File file = new File(sourceFolder, "test-file");
		FileUtils.write(file, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// add the destination folder
		File destFolder = new File(root, "dest-folder");
		destFolder.mkdir();
		ProcessTestUtil.uploadNewFile(client, destFolder, profileManager, fileManager, config);

		File destination = new File(destFolder, file.getName());

		// move the file
		ProcessTestUtil.moveFile(client, file, destination, profileManager, fileManager, config);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(destination, fileManager);
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(destFolder.getName(), fileNode.getParent().getName());

		// check that the new meta document has the file
		MetaDocument destParentMetaDocument = ProcessTestUtil.getMetaDocument(client, fileNode.getParent()
				.getKeyPair());
		MetaFolder parentFolder = (MetaFolder) destParentMetaDocument;
		Assert.assertEquals(1, parentFolder.getChildKeys().size());

		// check that the old meta document does not contain the file anymore
		MetaDocument sourceParentMetaDocument = ProcessTestUtil.getMetaDocument(client, userProfile
				.getFileByPath(sourceFolder, fileManager).getKeyPair());
		parentFolder = (MetaFolder) sourceParentMetaDocument;
		Assert.assertEquals(0, parentFolder.getChildKeys().size());
	}

	@Test
	public void testRename() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManager = new FileManager(root.toPath());

		// add a file to the network
		File file = new File(root, "test-file");
		FileUtils.write(file, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// don't move, only rename
		File destination = new File(root, "test-file-moved");

		// move the file
		ProcessTestUtil.moveFile(client, file, destination, profileManager, fileManager, config);

		// assert that the file is moved
		Assert.assertTrue(destination.exists());

		// check that the user profile has a correct entry
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(destination, fileManager);
		Assert.assertNotNull(fileNode);
		Assert.assertEquals(fileNode.getName(), destination.getName());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
