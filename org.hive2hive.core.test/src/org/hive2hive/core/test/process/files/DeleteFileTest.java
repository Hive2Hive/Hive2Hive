package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import net.tomp2p.futures.FutureGet;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.IH2HFileConfiguration;
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
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests uploading a new version of a file.
 * 
 * @author Nico
 * 
 */
public class DeleteFileTest extends H2HJUnitTest {

	private static final int networkSize = 2;
	private static List<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static IH2HFileConfiguration config = new TestH2HFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DeleteFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(network.get(0), userCredentials);

	}

	@Test
	public void testDeleteFile() throws IOException, IllegalFileLocation {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);

		// add a file to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root);
		File file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);

		// store the keys of the meta file to verify them later
		UserProfile userProfileBeforeDeletion = ProcessTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(file, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the file
		ProcessTestUtil.deleteFile(client, file, profileManager, fileManager);

		// check if the file is still in the DHT
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);

		MetaFile metaFileBeforeDeletion = (MetaFile) metaDocumentBeforeDeletion;
		for (FileVersion version : metaFileBeforeDeletion.getVersions()) {
			for (KeyPair key : version.getChunkIds()) {
				FutureGet get = client.getDataManager().getGlobal(ProcessStep.key2String(key.getPublic()),
						H2HConstants.FILE_CHUNK);
				get.awaitUninterruptibly();
				get.getFutureRequests().awaitUninterruptibly();

				// chunk should not exist
				Assert.assertNull(get.getData());
			}
		}
	}

	@Test
	public void testDeleteFileInFolder() throws IOException, IllegalFileLocation {
		NetworkManager client = network.get(1);
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		// add a folder to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root);
		File folder = new File(root, "test-folder");
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, manager, fileManager, config);

		// add a file to the network
		File file = new File(folder, "test-file");
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, file, manager, fileManager, config);

		// store some things to be able to test later
		UserProfile userProfileBeforeDeletion = ProcessTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPairFolder = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		KeyPair metaKeyPairFile = userProfileBeforeDeletion.getFileByPath(file, fileManager).getKeyPair();
		MetaFolder metaFolderBeforeDeletion = (MetaFolder) ProcessTestUtil.getMetaDocument(client,
				metaKeyPairFolder);
		MetaFile metaFileBeforeDeletion = (MetaFile) ProcessTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNotNull(metaFolderBeforeDeletion);
		Assert.assertNotNull(metaFileBeforeDeletion);
		Assert.assertEquals(1, metaFolderBeforeDeletion.getChildDocuments().size());

		// delete the file
		ProcessTestUtil.deleteFile(client, file, manager, fileManager);

		// check if the file is still in the DHT
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPairFile.getPublic()));

		// check if the folder is still in the DHT
		Assert.assertNotNull(userProfile.getFileById(metaKeyPairFolder.getPublic()));

		// check the meta file is still in the DHT
		MetaDocument metaFile = ProcessTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNull(metaFile);

		// check if the child is also gone
		MetaFolder metaFolder = (MetaFolder) ProcessTestUtil.getMetaDocument(client, metaKeyPairFolder);
		Assert.assertNotNull(metaFolder);
		Assert.assertEquals(0, metaFolder.getChildDocuments().size());
	}

	@Test
	public void testDeleteFolder() throws FileNotFoundException, IllegalFileLocation {
		NetworkManager client = network.get(1);
		UserProfileManager manager = new UserProfileManager(client, userCredentials);

		// add a folder to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root);
		File folder = new File(root, "test-folder");
		folder.mkdir();
		ProcessTestUtil.uploadNewFile(client, folder, manager, fileManager, config);

		// store some keys before deletion
		UserProfile userProfileBeforeDeletion = ProcessTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the folder
		ProcessTestUtil.deleteFile(client, folder, manager, fileManager);

		// check if the folder is still in the DHT
		UserProfile userProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
