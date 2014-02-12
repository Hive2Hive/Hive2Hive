package org.hive2hive.core.test.processes.implementations.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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

	private File uploaderRoot;
	private File downloaderRoot;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = AddFileTest.class;
		beforeClass();

	}

	@Before
	public void register() throws NoPeerConnectionException {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		String randomName = NetworkTestUtil.randomString();
		uploaderRoot = new File(System.getProperty("java.io.tmpdir"), randomName);

		// register and login a user (peer 0)
		UseCaseTestUtil.registerAndLogin(userCredentials, network.get(0), uploaderRoot);

		// other client to verify this
		downloaderRoot = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		UseCaseTestUtil.login(userCredentials, network.get(1), downloaderRoot);
	}

	@Test
	public void testUploadSingleChunk() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, NoPeerConnectionException {
		File file = FileTestUtil.createFileRandomContent(1, uploaderRoot, config);

		UseCaseTestUtil.uploadNewFile(network.get(0), file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadMultipleChunks() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, NoPeerConnectionException {
		// creates a file with length of at least 5 chunks
		File file = FileTestUtil.createFileRandomContent(5, uploaderRoot, config);

		UseCaseTestUtil.uploadNewFile(network.get(0), file);
		verifyUpload(file, 5);
	}

	@Test
	public void testUploadFolder() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, NoPeerConnectionException {
		File folder = new File(uploaderRoot, "folder1");
		folder.mkdirs();

		UseCaseTestUtil.uploadNewFile(network.get(0), folder);
		verifyUpload(folder, 0);
	}

	@Test
	public void testUploadFolderWithFile() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, NoPeerConnectionException {
		// create a container
		File folder = new File(uploaderRoot, "folder-with-file");
		folder.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folder);

		File file = new File(folder, "test-file");
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
		UseCaseTestUtil.uploadNewFile(network.get(0), file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadFolderWithFolder() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, NoPeerConnectionException {
		File folder = new File(uploaderRoot, "folder-with-folder");
		folder.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folder);

		File innerFolder = new File(uploaderRoot, "inner-folder");
		innerFolder.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), innerFolder);

		verifyUpload(innerFolder, 0);
	}

	@Test(expected = NoSessionException.class)
	public void testUploadNoSession() throws IOException, IllegalFileLocation, NoSessionException,
			InvalidProcessStateException, NoPeerConnectionException {
		// skip the login and continue with the newfile process
		NetworkManager client = network.get(2);

		File file = FileTestUtil.createFileRandomContent(1, uploaderRoot, config);
		IProcessComponent process = ProcessFactory.instance().createNewFileProcess(file, client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		UseCaseTestUtil.waitTillFailed(listener, 40);
	}

	private void verifyUpload(File originalFile, int expectedChunks) throws IOException, GetFailedException,
			NoSessionException, NoPeerConnectionException {
		// pick new client to test
		NetworkManager client = network.get(1);

		// test if there is something in the user profile
		UserProfile gotProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNotNull(gotProfile);

		Index node = gotProfile.getFileByPath(originalFile, uploaderRoot.toPath());
		Assert.assertNotNull(node);

		// verify the meta document
		KeyPair metaFileKeys = node.getFileKeys();
		MetaDocument metaDocument = UseCaseTestUtil.getMetaDocument(client, metaFileKeys);
		Assert.assertNotNull(metaDocument);
		if (originalFile.isFile()) {
			// get the meta file with the keys (decrypt it)
			MetaFile metaFile = (MetaFile) metaDocument;
			Assert.assertEquals(1, metaFile.getVersions().size());
			Assert.assertEquals(expectedChunks, metaFile.getVersions().get(0).getChunkKeys().size());
		}

		// verify the file (should have been downloaded automatically during the notification)
		Path relative = uploaderRoot.toPath().relativize(originalFile.toPath());
		File file = new File(downloaderRoot, relative.toString());

		// give some seconds for the file to download
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!file.exists());

		Assert.assertTrue(file.exists());
		if (originalFile.isFile()) {
			Assert.assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(file));
		}
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(uploaderRoot);
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
