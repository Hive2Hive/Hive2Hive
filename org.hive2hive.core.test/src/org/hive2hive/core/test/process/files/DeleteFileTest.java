package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests deleting a file.
 * 
 * @author Nico
 * 
 */
public class DeleteFileTest extends H2HJUnitTest {

	private static final int networkSize = 2;
	private static List<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static IFileConfiguration config = new TestFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DeleteFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(userCredentials, network.get(0));
	}

	@Test
	public void testDeleteFile() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);

		// add a file to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root.toPath());
		File file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// store the keys of the meta file to verify them later
		UserProfile userProfileBeforeDeletion = profileManager.getUserProfile(-1, false);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(file, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the file
		ProcessTestUtil.deleteFile(client, file, profileManager, fileManager, config);

		// check if the file is still in the DHT
		UserProfile userProfile = profileManager.getUserProfile(-2, false);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);

		MetaFile metaFileBeforeDeletion = (MetaFile) metaDocumentBeforeDeletion;
		for (FileVersion version : metaFileBeforeDeletion.getVersions()) {
			for (KeyPair key : version.getChunkIds()) {
				FutureGet get = client.getDataManager().get(
						Number160.createHash(ProcessStep.key2String(key.getPublic())),
						H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.FILE_CHUNK));
				get.awaitUninterruptibly();
				get.getFutureRequests().awaitUninterruptibly();

				// chunk should not exist
				Assert.assertNull(get.getData());
			}
		}
	}

	@Test
	public void testDeleteFolder() throws FileNotFoundException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		// add a folder to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root.toPath());
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, manager, fileManager, config);

		// store some keys before deletion
		UserProfile userProfileBeforeDeletion = manager.getUserProfile(-1, false);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the folder
		ProcessTestUtil.deleteFile(client, folder, manager, fileManager, config);

		// check if the folder is still in the DHT
		UserProfile userProfile = manager.getUserProfile(-2, false);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);
	}

	@Test
	public void testDeleteFileInFolder() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		// add a folder to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root.toPath());
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, manager, fileManager, config);

		// add a file to the network
		File file = new File(folder, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, file, manager, fileManager, config);

		// store some things to be able to test later
		UserProfile userProfileBeforeDeletion = manager.getUserProfile(-1, false);
		KeyPair metaKeyPairFolder = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		KeyPair metaKeyPairFile = userProfileBeforeDeletion.getFileByPath(file, fileManager).getKeyPair();
		MetaFolder metaFolderBeforeDeletion = (MetaFolder) ProcessTestUtil.getMetaDocument(client,
				metaKeyPairFolder);
		MetaFile metaFileBeforeDeletion = (MetaFile) ProcessTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNotNull(metaFolderBeforeDeletion);
		Assert.assertNotNull(metaFileBeforeDeletion);
		Assert.assertEquals(1, metaFolderBeforeDeletion.getChildKeys().size());

		// delete the file
		ProcessTestUtil.deleteFile(client, file, manager, fileManager, config);

		// check if the file is still in the DHT
		UserProfile userProfile = manager.getUserProfile(-2, false);
		Assert.assertNull(userProfile.getFileById(metaKeyPairFile.getPublic()));

		// check if the folder is still in the DHT
		Assert.assertNotNull(userProfile.getFileById(metaKeyPairFolder.getPublic()));

		// check the meta file is still in the DHT
		MetaDocument metaFile = ProcessTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNull(metaFile);

		// check if the child is also gone
		MetaFolder metaFolder = (MetaFolder) ProcessTestUtil.getMetaDocument(client, metaKeyPairFolder);
		Assert.assertNotNull(metaFolder);
		Assert.assertEquals(0, metaFolder.getChildKeys().size());
	}

	@Test
	public void testDeleteFolderInFolder() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException {
		NetworkManager client = network.get(1);
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		// add a folder to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root.toPath());
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, manager, fileManager, config);

		// add a 2nd folder to the network
		File innerFolder = new File(folder, "inner-folder");
		innerFolder.mkdir();
		ProcessTestUtil.uploadNewFile(client, innerFolder, manager, fileManager, config);

		// store some things to be able to test later
		UserProfile userProfileBeforeDeletion = manager.getUserProfile(-1, false);
		KeyPair metaKeyPairFolder = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		KeyPair metaKeyPairInnerFolder = userProfileBeforeDeletion.getFileByPath(innerFolder, fileManager)
				.getKeyPair();
		MetaFolder metaFolderBeforeDeletion = (MetaFolder) ProcessTestUtil.getMetaDocument(client,
				metaKeyPairFolder);
		MetaFolder metaInnerFolderBeforeDeletion = (MetaFolder) ProcessTestUtil.getMetaDocument(client,
				metaKeyPairInnerFolder);
		Assert.assertNotNull(metaFolderBeforeDeletion);
		Assert.assertNotNull(metaInnerFolderBeforeDeletion);
		Assert.assertEquals(1, metaFolderBeforeDeletion.getChildKeys().size());
		Assert.assertEquals(0, metaInnerFolderBeforeDeletion.getChildKeys().size());

		// delete the inner folder
		ProcessTestUtil.deleteFile(client, innerFolder, manager, fileManager, config);

		// check if the inner folder is still in the DHT
		UserProfile userProfile = manager.getUserProfile(-2, false);
		Assert.assertNull(userProfile.getFileById(metaKeyPairInnerFolder.getPublic()));

		// check if the outer folder is still in the DHT
		Assert.assertNotNull(userProfile.getFileById(metaKeyPairFolder.getPublic()));

		// check the inner meta folder is still in the DHT
		MetaDocument metaInnerFolder = ProcessTestUtil.getMetaDocument(client, metaKeyPairInnerFolder);
		Assert.assertNull(metaInnerFolder);

		// check if the child folder is also gone
		MetaFolder metaFolder = (MetaFolder) ProcessTestUtil.getMetaDocument(client, metaKeyPairFolder);
		Assert.assertNotNull(metaFolder);
		Assert.assertEquals(0, metaFolder.getChildKeys().size());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
