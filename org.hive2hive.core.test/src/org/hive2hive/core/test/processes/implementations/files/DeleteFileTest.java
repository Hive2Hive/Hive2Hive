package org.hive2hive.core.test.processes.implementations.files;

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
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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
	private static final IFileConfiguration config = new TestFileConfiguration();
	private static File root;
	private static NetworkManager client;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DeleteFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		client = network.get(0);

		// register a user
		UseCaseTestUtil.registerAndLogin(userCredentials, client, root);
	}

	@Test
	public void testDeleteFile() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoPeerConnectionException, NoSessionException {
		File file = FileTestUtil.createFileRandomContent(3, root, config);
		UseCaseTestUtil.uploadNewFile(client, file);

		// store the keys of the meta file to verify them later
		UserProfile userProfileBeforeDeletion = UseCaseTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(file, root).getFileKeys();
		MetaDocument metaDocumentBeforeDeletion = UseCaseTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the file
		UseCaseTestUtil.deleteFile(client, file);

		// check if the file is still in the DHT
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = UseCaseTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);

		MetaFile metaFileBeforeDeletion = (MetaFile) metaDocumentBeforeDeletion;
		for (FileVersion version : metaFileBeforeDeletion.getVersions()) {
			for (KeyPair key : version.getChunkKeys()) {
				FutureGet get = client.getDataManager().get(
						Number160.createHash(H2HEncryptionUtil.key2String(key.getPublic())),
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
			InterruptedException, NoSessionException, NoPeerConnectionException {
		// add a folder to the network
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		// store some keys before deletion
		UserProfile userProfileBeforeDeletion = UseCaseTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPair = userProfileBeforeDeletion.getFileByPath(folder, root).getFileKeys();
		MetaDocument metaDocumentBeforeDeletion = UseCaseTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNotNull(metaDocumentBeforeDeletion);

		// delete the folder
		UseCaseTestUtil.deleteFile(client, folder);

		// check if the folder is still in the DHT
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPair.getPublic()));

		MetaDocument metaDocument = UseCaseTestUtil.getMetaDocument(client, metaKeyPair);
		Assert.assertNull(metaDocument);
	}

	@Test
	public void testDeleteFileInFolder() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		// add a folder to the network
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		// add a file to the network
		File file = new File(folder, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(client, file);

		// store some things to be able to test later
		UserProfile userProfileBeforeDeletion = UseCaseTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPairFolder = userProfileBeforeDeletion.getFileByPath(folder, root).getFileKeys();
		KeyPair metaKeyPairFile = userProfileBeforeDeletion.getFileByPath(file, root).getFileKeys();
		MetaFolder metaFolderBeforeDeletion = (MetaFolder) UseCaseTestUtil.getMetaDocument(client,
				metaKeyPairFolder);
		MetaFile metaFileBeforeDeletion = (MetaFile) UseCaseTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNotNull(metaFolderBeforeDeletion);
		Assert.assertNotNull(metaFileBeforeDeletion);
		Assert.assertEquals(1, metaFolderBeforeDeletion.getChildKeys().size());

		// delete the file
		UseCaseTestUtil.deleteFile(client, file);

		// check if the file is still in the DHT
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPairFile.getPublic()));

		// check if the folder is still in the DHT
		Assert.assertNotNull(userProfile.getFileById(metaKeyPairFolder.getPublic()));

		// check the meta file is still in the DHT
		MetaDocument metaFile = UseCaseTestUtil.getMetaDocument(client, metaKeyPairFile);
		Assert.assertNull(metaFile);

		// check if the child is also gone
		MetaFolder metaFolder = (MetaFolder) UseCaseTestUtil.getMetaDocument(client, metaKeyPairFolder);
		Assert.assertNotNull(metaFolder);
		Assert.assertEquals(0, metaFolder.getChildKeys().size());
	}

	@Test
	public void testDeleteFolderInFolder() throws IOException, IllegalFileLocation, GetFailedException,
			InterruptedException, NoSessionException, NoPeerConnectionException {
		// add a folder to the network
		File folder = new File(root, NetworkTestUtil.randomString());
		folder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, folder);

		// add a 2nd folder to the network
		File innerFolder = new File(folder, "inner-folder");
		innerFolder.mkdir();
		UseCaseTestUtil.uploadNewFile(client, innerFolder);

		// store some things to be able to test later
		UserProfile userProfileBeforeDeletion = UseCaseTestUtil.getUserProfile(client, userCredentials);
		KeyPair metaKeyPairFolder = userProfileBeforeDeletion.getFileByPath(folder, root).getFileKeys();
		KeyPair metaKeyPairInnerFolder = userProfileBeforeDeletion.getFileByPath(innerFolder, root)
				.getFileKeys();
		MetaFolder metaFolderBeforeDeletion = (MetaFolder) UseCaseTestUtil.getMetaDocument(client,
				metaKeyPairFolder);
		MetaFolder metaInnerFolderBeforeDeletion = (MetaFolder) UseCaseTestUtil.getMetaDocument(client,
				metaKeyPairInnerFolder);
		Assert.assertNotNull(metaFolderBeforeDeletion);
		Assert.assertNotNull(metaInnerFolderBeforeDeletion);
		Assert.assertEquals(1, metaFolderBeforeDeletion.getChildKeys().size());
		Assert.assertEquals(0, metaInnerFolderBeforeDeletion.getChildKeys().size());

		// delete the inner folder
		UseCaseTestUtil.deleteFile(client, innerFolder);

		// check if the inner folder is still in the DHT
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNull(userProfile.getFileById(metaKeyPairInnerFolder.getPublic()));

		// check if the outer folder is still in the DHT
		Assert.assertNotNull(userProfile.getFileById(metaKeyPairFolder.getPublic()));

		// check the inner meta folder is still in the DHT
		MetaDocument metaInnerFolder = UseCaseTestUtil.getMetaDocument(client, metaKeyPairInnerFolder);
		Assert.assertNull(metaInnerFolder);

		// check if the child folder is also gone
		MetaFolder metaFolder = (MetaFolder) UseCaseTestUtil.getMetaDocument(client, metaKeyPairFolder);
		Assert.assertNotNull(metaFolder);
		Assert.assertEquals(0, metaFolder.getChildKeys().size());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
