package org.hive2hive.processes.test.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.processes.ProcessFactory;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.test.util.TestProcessComponentListener;
import org.hive2hive.processes.test.util.UseCaseTestUtil;
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
public class AddFileTest extends H2HJUnitTest {

	private final int networkSize = 3;
	private List<NetworkManager> network;
	private IFileConfiguration config = new TestFileConfiguration();
	private UserCredentials userCredentials;
	private File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = AddFileTest.class;
		beforeClass();

	}

	@Before
	public void register() {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		String randomName = NetworkTestUtil.randomString();
		root = new File(System.getProperty("java.io.tmpdir"), randomName);

		// register and login a user
		UseCaseTestUtil.register(userCredentials, network.get(0));
	}

	@Test
	public void testUploadSingleChunk() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		File file = FileTestUtil.createFileRandomContent(1, root, config);

		startUploadProcess(file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadMultipleChunks() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		// creates a file with length of at least 5 chunks
		File file = FileTestUtil.createFileRandomContent(5, root, config);

		startUploadProcess(file);
		verifyUpload(file, 5);
	}

	@Test
	public void testUploadFolder() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		File folder = new File(root, "folder1");
		folder.mkdirs();

		startUploadProcess(folder);
		verifyUpload(folder, 0);
	}

	@Test
	public void testUploadFolderWithFile() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		// create a container
		File folder = new File(root, "folder-with-file");
		folder.mkdirs();
		startUploadProcess(folder);

		File file = new File(folder, "test-file");
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		startUploadProcess(file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadFolderWithFolder() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		File folder = new File(root, "folder-with-folder");
		folder.mkdirs();
		startUploadProcess(folder);

		File innerFolder = new File(root, "inner-folder");
		innerFolder.mkdir();
		startUploadProcess(innerFolder);

		verifyUpload(innerFolder, 0);
	}

	@Test(expected = NoSessionException.class)
	public void testUploadNoSession() throws IOException, IllegalFileLocation, NoSessionException,
			InvalidProcessStateException {
		// skip the login and continue with the newfile process
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		File file = FileTestUtil.createFileRandomContent(1, root, config);
		IProcessComponent process = ProcessFactory.instance().createNewFileProcess(file, client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		UseCaseTestUtil.waitTillFailed(listener, 40);
	}

	private void startUploadProcess(File toUpload) throws IllegalFileLocation, NoSessionException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UseCaseTestUtil.login(userCredentials, client, root);
		UseCaseTestUtil.uploadNewFile(client, toUpload);
	}

	private void verifyUpload(File originalFile, int expectedChunks) throws IOException, GetFailedException,
			NoSessionException {
		// pick new client to test
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UseCaseTestUtil.login(userCredentials, client, FileUtils.getTempDirectory());

		// test if there is something in the user profile
		UserProfile gotProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNotNull(gotProfile);

		FileTreeNode node = gotProfile.getFileByPath(originalFile, new FileManager(root.toPath()));
		Assert.assertNotNull(node);

		// verify the meta document
		KeyPair metaFileKeys = node.getKeyPair();
		MetaDocument metaDocument = ProcessTestUtil.getMetaDocument(client, metaFileKeys);
		if (originalFile.isFile()) {
			// get the meta file with the keys (decrypt it)
			MetaFile metaFile = (MetaFile) metaDocument;
			Assert.assertNotNull(metaFile);
			Assert.assertEquals(1, metaFile.getVersions().size());
			Assert.assertEquals(expectedChunks, metaFile.getVersions().get(0).getChunkKeys().size());
		} else {
			// get meta folder
			MetaFolder metaFolder = (MetaFolder) metaDocument;
			Assert.assertNotNull(metaFolder);
			Assert.assertEquals(originalFile.list().length, metaFolder.getChildKeys().size());
		}

		// verify the file after downloadig it
		File file = UseCaseTestUtil.downloadFile(client, node.getKeyPair().getPublic());
		Assert.assertTrue(file.exists());
		if (originalFile.isFile()) {
			Assert.assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(file));
		}
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(root);
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
