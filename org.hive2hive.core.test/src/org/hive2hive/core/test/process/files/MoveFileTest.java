package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hive2hive.core.IH2HFileConfiguration;
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
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
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
	private static UserCredentials userCredentials;
	private static IH2HFileConfiguration config = new TestH2HFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MoveFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
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
		FileManager fileManager = new FileManager(root);
		File file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// add the target directory
		File folder = new File(fileManager.getRoot(), "folder");
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

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
