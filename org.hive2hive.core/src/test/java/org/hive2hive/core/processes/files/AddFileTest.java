package org.hive2hive.core.processes.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests uploading a new file.
 * 
 * @author Nico, Seppi
 */
public class AddFileTest extends H2HJUnitTest {

	private final static int CHUNK_SIZE = 1024;
	private final static int NETWORK_SIZE = 3;

	private static List<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static File uploaderRoot;
	private static File downloaderRoot;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = AddFileTest.class;
		beforeClass();
		// setup network
		network = NetworkTestUtil.createNetwork(NETWORK_SIZE);
		// create some random user credentials
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		// register and login a user (peer 0)
		uploaderRoot = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.registerAndLogin(userCredentials, network.get(0), uploaderRoot);

		// other client to verify this
		downloaderRoot = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.login(userCredentials, network.get(1), downloaderRoot);
	}

	@Test
	public void testUploadSingleChunk() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			NoPeerConnectionException, InvalidProcessStateException {
		File file = FileTestUtil.createFileRandomContent(1, uploaderRoot, CHUNK_SIZE);

		UseCaseTestUtil.uploadNewFile(network.get(0), file);
		verifyUpload(file, 1);
	}

	@Test
	public void testUploadMultipleChunks() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			NoPeerConnectionException, InvalidProcessStateException {
		// creates a file with length of at least 5 chunks
		File file = FileTestUtil.createFileRandomContent(5, uploaderRoot, CHUNK_SIZE);

		UseCaseTestUtil.uploadNewFile(network.get(0), file);
		verifyUpload(file, 5);
	}

	@Test
	public void testUploadFolder() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			NoPeerConnectionException, InvalidProcessStateException {
		File folder = new File(uploaderRoot, "folder1");
		folder.mkdirs();

		UseCaseTestUtil.uploadNewFile(network.get(0), folder);
		verifyUpload(folder, 0);
	}

	@Test
	public void testUploadFolderWithFile() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			NoPeerConnectionException, InvalidProcessStateException {
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
			GetFailedException, NoPeerConnectionException, InvalidProcessStateException {
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

		File file = FileTestUtil.createFileRandomContent(1, uploaderRoot, CHUNK_SIZE);
		IProcessComponent process = ProcessFactory.instance().createNewFileProcess(file, client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		TestExecutionUtil.waitTillFailed(listener, 40);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadNull() throws NoSessionException, NoPeerConnectionException {
		ProcessFactory.instance().createNewFileProcess(null, network.get(0));
	}

	private void verifyUpload(File originalFile, int expectedChunks) throws IOException, GetFailedException,
			NoSessionException, NoPeerConnectionException, InvalidProcessStateException {
		// pick new client to test
		NetworkManager client = network.get(1);

		// test if there is something in the user profile
		UserProfile gotProfile = UseCaseTestUtil.getUserProfile(client, userCredentials);
		Assert.assertNotNull(gotProfile);

		Index node = gotProfile.getFileByPath(originalFile, uploaderRoot.toPath());
		Assert.assertNotNull(node);

		// verify the meta document
		KeyPair metaFileKeys = node.getFileKeys();
		if (originalFile.isFile()) {
			MetaFile metaFile = UseCaseTestUtil.getMetaFile(client, metaFileKeys);
			Assert.assertNotNull(metaFile);
			Assert.assertTrue(metaFile instanceof MetaFileSmall);
			MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;

			// get the meta file with the keys (decrypt it)
			Assert.assertEquals(1, metaFileSmall.getVersions().size());
			Assert.assertEquals(expectedChunks, metaFileSmall.getVersions().get(0).getMetaChunks().size());
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

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(uploaderRoot);
		FileUtils.deleteDirectory(downloaderRoot);
		afterClass();
	}
}
