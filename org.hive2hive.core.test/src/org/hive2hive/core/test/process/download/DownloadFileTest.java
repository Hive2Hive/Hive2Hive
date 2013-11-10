package org.hive2hive.core.test.process.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.UploadFileProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests downloading a file.
 * 
 * @author Nico
 * 
 */
public class DownloadFileTest extends H2HJUnitTest {

	private final static int networkSize = 10;
	private static List<NetworkManager> network;
	private final static String testContent = "DownloadFileTest";
	private static FileManager uploaderFileManager;
	private static File uploadedFile;
	private static FileTreeNode file;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DownloadFileTest.class;
		beforeClass();

		/** create a network, register a user and add a file **/
		network = NetworkTestUtil.createNetwork(networkSize);
		UserCredentials userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		RegisterProcess registerProcess = new RegisterProcess(userCredentials, network.get(0));
		TestProcessListener listener = new TestProcessListener();
		registerProcess.addListener(listener);
		registerProcess.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		UserProfile userProfile = registerProcess.getContext().getUserProfile();
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		uploaderFileManager = new FileManager(root);

		// upload a file
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		String fileName = NetworkTestUtil.randomString();
		uploadedFile = new File(root, fileName);
		FileUtils.write(uploadedFile, testContent);
		UploadFileProcess ulProcess = new UploadFileProcess(uploadedFile, userProfile, userCredentials,
				client, uploaderFileManager, new TestH2HFileConfiguration());

		listener = new TestProcessListener();
		ulProcess.addListener(listener);
		ulProcess.start();

		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		file = userProfile.getRoot().getChildByName(fileName);
	}

	@Test
	public void testDownload() throws IOException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		File newRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager downloaderFileManager = new FileManager(newRoot);

		DownloadFileProcess process = new DownloadFileProcess(file, client, downloaderFileManager);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// the downloaded file should now be on the disk
		File downloadedFile = new File(newRoot, file.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(uploaderFileManager.getRoot());
		afterClass();
	}
}
