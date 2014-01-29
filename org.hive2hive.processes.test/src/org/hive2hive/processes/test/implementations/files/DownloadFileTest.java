package org.hive2hive.processes.test.implementations.files;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.processes.ProcessFactory;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.test.util.TestProcessComponentListener;
import org.hive2hive.processes.test.util.UseCaseTestUtil;
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
	private FileTreeNode fileNode;
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
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// register and login
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		UseCaseTestUtil.registerAndLogin(userCredentials, client, root);

		// upload a file
		String fileName = NetworkTestUtil.randomString();
		uploadedFile = new File(root, fileName);
		FileUtils.write(uploadedFile, testContent);
		UseCaseTestUtil.uploadNewFile(client, uploadedFile);

		UserProfile up = UseCaseTestUtil.getUserProfile(client, userCredentials);
		fileNode = up.getRoot().getChildByName(fileName);
	}

	@Test
	public void testDownload() throws IOException, NoSessionException, GetFailedException {
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UseCaseTestUtil.login(userCredentials, client, newRoot);

		UseCaseTestUtil.downloadFile(client, fileNode.getFileKey());

		// the downloaded file should now be on the disk
		File downloadedFile = new File(newRoot, fileNode.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);
	}

	@Test
	public void testDownloadWrongKeys() throws IOException, NoSessionException, GetFailedException,
			InvalidProcessStateException {
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UseCaseTestUtil.login(userCredentials, client, newRoot);

		KeyPair wrongKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);

		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(
				wrongKeys.getPublic(), client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		UseCaseTestUtil.waitTillFailed(listener, 20);
	}

	@Test
	public void testDownloadFileAlreadyExisting() throws IOException, NoSessionException, GetFailedException {
		// should overwrite the existing file
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// create the existing file
		File existing = new File(newRoot, uploadedFile.getName());
		FileUtils.write(existing, "existing content");
		byte[] md5Before = EncryptionUtil.generateMD5Hash(existing);

		UseCaseTestUtil.login(userCredentials, client, newRoot);
		UseCaseTestUtil.downloadFile(client, fileNode.getFileKey());

		// the downloaded file should still be on the disk
		File downloadedFile = new File(newRoot, fileNode.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);

		// the content of the existing file is modified
		Assert.assertFalse(H2HEncryptionUtil.compareMD5(downloadedFile, md5Before));
	}

	@Test
	public void testDownloadFileAlreadyExistingSameContent() throws IOException, NoSessionException,
			InvalidProcessStateException {
		// should not overwrite the existing file
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		NetworkManager client = network.get(new Random().nextInt(networkSize));

		// create the existing file
		File existing = new File(newRoot, uploadedFile.getName());
		FileUtils.write(existing, testContent);
		long lastModifiedBefore = existing.lastModified();

		UseCaseTestUtil.login(userCredentials, client, newRoot);
		IProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(
				fileNode.getFileKey(), client);
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
