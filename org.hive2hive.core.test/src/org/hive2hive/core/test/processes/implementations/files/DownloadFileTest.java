package org.hive2hive.core.test.processes.implementations.files;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IndexNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.DenyingMessageReplyHandler;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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
	private static List<NetworkManager> network;

	private File uploadedFile;
	private IndexNode fileNode;
	private UserCredentials userCredentials;
	private File downloaderRoot;
	private NetworkManager downloader;

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
		NetworkManager uploader = network.get(networkSize - 1);
		downloader = network.get(new Random().nextInt(networkSize - 2));

		// register and login both users
		File uploaderRoot = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		UseCaseTestUtil.registerAndLogin(userCredentials, uploader, uploaderRoot);
		downloaderRoot = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		UseCaseTestUtil.login(userCredentials, downloader, downloaderRoot);

		// workaround that the downloader does not get notified about the newly added file (it will be done
		// manually)
		downloader.getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());

		// upload a file
		String fileName = NetworkTestUtil.randomString();
		uploadedFile = new File(uploaderRoot, fileName);
		FileUtils.write(uploadedFile, testContent);
		UseCaseTestUtil.uploadNewFile(uploader, uploadedFile);

		UserProfile up = UseCaseTestUtil.getUserProfile(uploader, userCredentials);
		fileNode = up.getRoot().getChildByName(fileName);

	}

	@Test
	public void testDownload() throws IOException, NoSessionException, GetFailedException {
		UseCaseTestUtil.downloadFile(downloader, fileNode.getFileKey());

		// the downloaded file should now be on the disk
		File downloadedFile = new File(downloaderRoot, fileNode.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);
	}

	@Test
	public void testDownloadWrongKeys() throws IOException, NoSessionException, GetFailedException,
			InvalidProcessStateException {
		KeyPair wrongKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);

		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(
				wrongKeys.getPublic(), downloader);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		UseCaseTestUtil.waitTillFailed(listener, 20);
	}

	@Test
	// should overwrite the existing file
	public void testDownloadFileAlreadyExisting() throws IOException, NoSessionException, GetFailedException {
		// create the existing file
		File existing = new File(downloaderRoot, uploadedFile.getName());
		FileUtils.write(existing, "existing content");
		byte[] md5Before = EncryptionUtil.generateMD5Hash(existing);

		UseCaseTestUtil.downloadFile(downloader, fileNode.getFileKey());

		// the downloaded file should still be on the disk
		File downloadedFile = new File(downloaderRoot, fileNode.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);

		// the content of the existing file is modified
		Assert.assertFalse(H2HEncryptionUtil.compareMD5(downloadedFile, md5Before));
	}

	@Test
	// should NOT overwrite the existing file
	public void testDownloadFileAlreadyExistingSameContent() throws IOException, NoSessionException,
			InvalidProcessStateException {
		// create the existing file
		File existing = new File(downloaderRoot, uploadedFile.getName());
		FileUtils.write(existing, testContent);
		long lastModifiedBefore = existing.lastModified();

		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(
				fileNode.getFileKey(), downloader);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		UseCaseTestUtil.waitTillFailed(listener, 20);

		// the existing file has already same content, should not have been downloaded
		Assert.assertEquals(lastModifiedBefore, existing.lastModified());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
