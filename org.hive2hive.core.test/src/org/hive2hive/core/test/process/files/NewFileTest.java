package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
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
 * Tests uploading a new file.
 * 
 * @author Nico
 * 
 */
public class NewFileTest extends H2HJUnitTest {

	private final int networkSize = 5;
	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private FileManager fileManager;
	private IH2HFileConfiguration config = new TestH2HFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NewFileTest.class;
		beforeClass();

	}

	@Before
	public void createProfile() {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(network.get(0), userCredentials);

		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root);
	}

	@Test
	public void testUploadSingleChunk() throws IOException, IllegalFileLocation {
		File file = FileTestUtil.createFileRandomContent(1, fileManager, config);

		startUploadProcess(file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadMultipleChunks() throws IOException, IllegalFileLocation {
		// creates a file with length of at least 5 chunks
		File file = FileTestUtil.createFileRandomContent(5, fileManager, config);

		startUploadProcess(file);
		verifyUpload(file, 5);
	}

	@Test
	public void testUploadFolder() throws IOException, IllegalFileLocation {
		File folder = new File(fileManager.getRoot(), "folder1");
		folder.mkdirs();

		startUploadProcess(folder);
		verifyUpload(folder, 0);
	}

	@Test
	public void testUploadFolderWithFile() throws IOException, IllegalFileLocation {
		// create a container
		File folder = new File(fileManager.getRoot(), "folder-with-file");
		folder.mkdirs();
		startUploadProcess(folder);

		File file = new File(folder, "test-file");
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		startUploadProcess(file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadFolderWithFolder() throws IOException, IllegalFileLocation {
		File folder = new File(fileManager.getRoot(), "folder-with-folder");
		folder.mkdirs();
		startUploadProcess(folder);

		File innerFolder = new File(fileManager.getRoot(), "inner-folder");
		innerFolder.mkdir();
		startUploadProcess(innerFolder);

		verifyUpload(innerFolder, 0);
	}

	private void startUploadProcess(File toUpload) throws IllegalFileLocation {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		NewFileProcess process = new NewFileProcess(toUpload, userCredentials, client, fileManager, config);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait maximally 1m because files could be large
		H2HWaiter waiter = new H2HWaiter(60);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

	}

	private void verifyUpload(File originalFile, int expectedChunks) throws IOException {
		// pick new client to test
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// test if there is something in the user profile
		UserProfile gotProfile = ProcessTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNotNull(gotProfile);

		FileTreeNode node = gotProfile.getFileByPath(originalFile, fileManager);
		Assert.assertNotNull(node);

		// verify the meta document
		KeyPair metaFileKeys = node.getKeyPair();
		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaFileKeys);
		if (originalFile.isFile()) {
			// get the meta file with the keys (decrypt it)
			MetaFile metaFile = (MetaFile) metaDocument;
			Assert.assertNotNull(metaFile);
			Assert.assertEquals(1, metaFile.getVersions().size());
			Assert.assertEquals(expectedChunks, metaFile.getVersions().get(0).getChunkIds().size());
		} else {
			// get meta folder
			MetaFolder metaFolder = (MetaFolder) metaDocument;
			Assert.assertNotNull(metaFolder);
			Assert.assertEquals(originalFile.list().length, metaFolder.getChildDocuments().size());
		}

		// create new filemanager
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManager2 = new FileManager(root);

		// verify the file after downloadig it
		File file = ProcessTestUtil.downloadFile(client, node, fileManager2);
		Assert.assertTrue(file.exists());
		if (originalFile.isFile()) {
			Assert.assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(file));
		}
	}

	@Test
	public void testUploadWrongCredentials() throws IOException, IllegalFileLocation {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		File file = FileTestUtil.createFileRandomContent(1, fileManager, config);

		NetworkManager client = network.get(new Random().nextInt(networkSize));
		NewFileProcess process = new NewFileProcess(file, userCredentials, client, fileManager, config);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait maximally 1m because files could be large
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManager.getRoot());
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
