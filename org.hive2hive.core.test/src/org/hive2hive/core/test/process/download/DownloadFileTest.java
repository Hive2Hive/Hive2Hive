package org.hive2hive.core.test.process.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
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
 * Tests downloading a file.
 * 
 * @author Nico
 * 
 */
public class DownloadFileTest extends H2HJUnitTest {

	private final static int networkSize = 5;
	private final static String testContent = "DownloadFileTest";
	private final TestFileConfiguration config = new TestFileConfiguration();
	private static List<NetworkManager> network;
	private FileManager uploaderFileManager;
	private File uploadedFile;
	private FileTreeNode file;
	private UserCredentials userCredentials;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DownloadFileTest.class;
		beforeClass();

		/** create a network, register a user and add a file **/
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void uploadFile() throws Exception {
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(userCredentials, network.get(0));

		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		uploaderFileManager = new FileManager(root.toPath());

		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);

		// upload a file
		String fileName = NetworkTestUtil.randomString();
		uploadedFile = new File(root, fileName);
		FileUtils.write(uploadedFile, testContent);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, uploaderFileManager));
		NewFileProcess ulProcess = new NewFileProcess(uploadedFile, client);

		TestProcessListener listener = new TestProcessListener();
		ulProcess.addListener(listener);
		ulProcess.start();

		H2HWaiter waiter = new H2HWaiter(30);
		do {
			Assert.assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		UserProfile up = profileManager.getUserProfile(ulProcess.getID(), false);
		file = up.getRoot().getChildByName(fileName);
	}

	@Test
	public void testDownload() throws IOException, NoSessionException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager downloaderFileManager = new FileManager(newRoot.toPath());

		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, downloaderFileManager));
		DownloadFileProcess process = new DownloadFileProcess(file, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			Assert.assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// the downloaded file should now be on the disk
		File downloadedFile = new File(newRoot, file.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);
	}

	@Test
	public void testDownloadWrongKeys() throws IOException, NoSessionException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager downloaderFileManager = new FileManager(newRoot.toPath());

		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, downloaderFileManager));
		FileTreeNode wrongKeys = new FileTreeNode(file.getParent(),
				EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT_RSA), "bla",
				"bla".getBytes());
		DownloadFileProcess process = new DownloadFileProcess(wrongKeys, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			Assert.assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Test
	public void testDownloadFileAlreadyExisting() throws IOException, NoSessionException {
		// should overwrite the existing file
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager downloaderFileManager = new FileManager(newRoot.toPath());

		// create the existing file
		File existing = new File(downloaderFileManager.getRoot().toFile(), uploadedFile.getName());
		FileUtils.write(existing, "existing content");
		byte[] md5Before = EncryptionUtil.generateMD5Hash(existing);

		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, downloaderFileManager));

		DownloadFileProcess process = new DownloadFileProcess(file, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			Assert.assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// the downloaded file should still be on the disk
		File downloadedFile = new File(newRoot, file.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);

		// the content of the existing file is modified
		Assert.assertFalse(H2HEncryptionUtil.compareMD5(downloadedFile, md5Before));
	}

	@Test
	public void testDownloadFileAlreadyExistingSameContent() throws IOException, NoSessionException {
		// should overwrite the existing file
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager downloaderFileManager = new FileManager(newRoot.toPath());

		// create the existing file
		File existing = new File(downloaderFileManager.getRoot().toFile(), uploadedFile.getName());
		FileUtils.write(existing, testContent);
		long lastModifiedBefore = existing.lastModified();

		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, downloaderFileManager));
		DownloadFileProcess process = new DownloadFileProcess(file, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			Assert.assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// the existing file has already same content, should not have been downloaded
		Assert.assertEquals(lastModifiedBefore, existing.lastModified());
	}

	@After
	public void delete() throws IOException {
		FileUtils.deleteDirectory(uploaderFileManager.getRoot().toFile());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
