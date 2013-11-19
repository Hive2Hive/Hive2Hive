package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import net.tomp2p.futures.FutureGet;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkPutGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
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
		NetworkPutGetUtil.register(network.get(0), userCredentials);

	}

	@Test
	public void testDeleteFile() throws IOException {
		NetworkManager client = network.get(0);

		// add a file to the network
		// create a file
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root);
		File file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		NetworkPutGetUtil.uploadNewFile(client, file, userCredentials, fileManager, config);

		// store the keys of the meta file to verify them later
		UserProfile userProfileBeforeDeletion = NetworkPutGetUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(file, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = NetworkPutGetUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the file
		NetworkPutGetUtil.deleteFile(client, file, userCredentials, fileManager);

		// check if the file is still in the DHT
		UserProfile userProfile = NetworkPutGetUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = NetworkPutGetUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);

		MetaFile metaFileBeforeDeletion = (MetaFile) metaDocumentBeforeDeletion;
		for (FileVersion version : metaFileBeforeDeletion.getVersions()) {
			for (KeyPair key : version.getChunkIds()) {
				FutureGet get = client.getGlobal(ProcessStep.key2String(key.getPublic()),
						H2HConstants.FILE_CHUNK);
				get.awaitUninterruptibly();
				get.getFutureRequests().awaitUninterruptibly();

				// chunk should not exist
				Assert.assertNull(get.getData());
			}
		}
	}

	@Test
	public void testDeleteFolder() throws FileNotFoundException {
		NetworkManager client = network.get(0);

		// add a folder to the network
		// create a file
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		FileManager fileManager = new FileManager(root);
		File folder = new File(root, "test-folder");
		folder.mkdir();
		NetworkPutGetUtil.uploadNewFile(client, folder, userCredentials, fileManager, config);

		UserProfile userProfileBeforeDeletion = NetworkPutGetUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(folder, fileManager).getKeyPair();
		MetaDocument metaDocumentBeforeDeletion = NetworkPutGetUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the folder
		NetworkPutGetUtil.deleteFile(client, folder, userCredentials, fileManager);

		// check if the folder is still in the DHT
		UserProfile userProfile = NetworkPutGetUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = NetworkPutGetUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
