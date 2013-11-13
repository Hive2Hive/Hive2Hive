package org.hive2hive.core.test.process.upload;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkPutGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests uploading a new version of a file.
 * 
 * @author Nico
 * 
 */
public class NewVersionTest extends H2HJUnitTest {

	private final int networkSize = 10;
	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private FileManager fileManager;
	private IH2HFileConfiguration config = new TestH2HFileConfiguration();
	private String originalContent;
	private File file;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NewVersionTest.class;
		beforeClass();

	}

	@Before
	public void createProfileUploadBaseFile() throws IOException {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		NetworkPutGetUtil.register(network.get(0), userCredentials);

		// create a file
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root);
		file = FileTestUtil.createFileRandomContent(3, fileManager, config);
		originalContent = FileUtils.readFileToString(file);
		NetworkPutGetUtil.uploadNewFile(network.get(0), file, userCredentials, fileManager, config);
	}

	@Test
	public void testUploadNewVersion() throws IOException {
		NetworkManager uploader = network.get(1);
		NetworkManager downloader = network.get(2);

		{
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			FileManager downloaderFileManager = new FileManager(root);

			UserProfile userProfile = NetworkPutGetUtil.getUserProfile(downloader, userCredentials);
			FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);

			// verify the original content
			File downloaded = NetworkPutGetUtil.downloadFile(downloader, fileNode, downloaderFileManager);
			Assert.assertEquals(originalContent, FileUtils.readFileToString(downloaded));
		}

		{
			// overwrite the content in the file
			String newContent = NetworkTestUtil.randomString();
			FileUtils.write(file, newContent, false);

			// upload the new version
			NetworkPutGetUtil.uploadNewFileVersion(uploader, file, userCredentials, fileManager, config);

			// use different file manager for not overriding the original file
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			FileManager downloaderFileManager = new FileManager(root);

			// download the file and check if version is newer
			UserProfile userProfile = NetworkPutGetUtil.getUserProfile(downloader, userCredentials);
			FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);
			File downloaded = NetworkPutGetUtil.downloadFile(downloader, fileNode, downloaderFileManager);

			// new content should be latest one
			Assert.assertEquals(newContent, FileUtils.readFileToString(downloaded));
		}
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
