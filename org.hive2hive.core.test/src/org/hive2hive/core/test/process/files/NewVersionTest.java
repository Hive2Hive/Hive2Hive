package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
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
 * Tests uploading a new version of a file.
 * 
 * @author Nico
 * 
 */
public class NewVersionTest extends H2HJUnitTest {

	private final int networkSize = 5;
	private final IFileConfiguration config = new TestFileConfiguration();
	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private FileManager fileManager;
	private String originalContent;
	private File file;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NewVersionTest.class;
		beforeClass();

	}

	@Before
	public void createProfileUploadBaseFile() throws IOException, IllegalFileLocation {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(userCredentials, network.get(0));

		// create a file
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root.toPath());
		file = FileTestUtil.createFileRandomContent(3, root, config);
		originalContent = FileUtils.readFileToString(file);
		ProcessTestUtil.uploadNewFile(network.get(0), file, new UserProfileManager(network.get(0),
				userCredentials), fileManager, config);
	}

	@Test
	public void testUploadNewVersion() throws IOException, GetFailedException {
		NetworkManager uploader = network.get(1);
		NetworkManager downloader = network.get(2);

		{
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			FileManager downloaderFileManager = new FileManager(root.toPath());

			UserProfile userProfile = ProcessTestUtil.getUserProfile(downloader, userCredentials);
			FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);

			// verify the original content
			UserProfileManager profileManager = new UserProfileManager(downloader, userCredentials);
			File downloaded = ProcessTestUtil.downloadFile(downloader, fileNode, profileManager,
					downloaderFileManager, config);
			Assert.assertEquals(originalContent, FileUtils.readFileToString(downloaded));
		}

		{
			// overwrite the content in the file
			String newContent = NetworkTestUtil.randomString();
			FileUtils.write(file, newContent, false);
			byte[] md5UpdatedFile = EncryptionUtil.generateMD5Hash(file);

			// upload the new version
			ProcessTestUtil.uploadNewFileVersion(uploader, file, new UserProfileManager(uploader,
					userCredentials), fileManager, config);

			// use different file manager for not overriding the original file
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			FileManager downloaderFileManager = new FileManager(root.toPath());

			// download the file and check if version is newer
			UserProfileManager profileManager = new UserProfileManager(downloader, userCredentials);
			UserProfile userProfile = profileManager.getUserProfile(-1, false);
			FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);
			File downloaded = ProcessTestUtil.downloadFile(downloader, fileNode, profileManager,
					downloaderFileManager, config);

			// new content should be latest one
			Assert.assertEquals(newContent, FileUtils.readFileToString(downloaded));

			// check the md5 hash
			Assert.assertTrue(H2HEncryptionUtil.compareMD5(downloaded, md5UpdatedFile));
		}
	}

	@Test
	public void testUploadSameVersion() throws IllegalFileLocation, GetFailedException, IOException,
			NoSessionException {
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		client.setSession(new H2HSession(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				profileManager, config, fileManager));

		// upload the same content again
		NewVersionProcess process = new NewVersionProcess(file, client);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(6000);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		// verify if the md5 hash did not change
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);
		Assert.assertTrue(H2HEncryptionUtil.compareMD5(file, fileNode.getMD5()));

		// verify that only one version was created
		MetaFile metaDocument = (MetaFile) ProcessTestUtil.getMetaDocument(client, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@Test
	public void testNewFolderVersion() throws IllegalFileLocation {
		// new folder version is illegal
		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);

		File folder = new File(fileManager.getRoot().toFile(), "test-folder");
		folder.mkdir();

		// upload the file
		ProcessTestUtil.uploadNewFile(client, folder, profileManager, fileManager, config);

		try {
			ProcessTestUtil.uploadNewFileVersion(client, folder, profileManager, fileManager, config);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// intended exception
		}
	}

	@Test
	public void testCleanupMaxNumVersions() throws IOException, GetFailedException {
		// overwrite config
		IFileConfiguration limitingConfig = new IFileConfiguration() {

			@Override
			public int getMaxSizeAllVersions() {
				return Integer.MAX_VALUE;
			}

			@Override
			public int getMaxNumOfVersions() {
				return 1;
			}

			@Override
			public int getMaxFileSize() {
				return Integer.MAX_VALUE;
			}

			@Override
			public int getChunkSize() {
				return H2HConstants.DEFAULT_CHUNK_SIZE;
			}
		};

		// update the file
		FileUtils.write(file, "bla", false);

		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		ProcessTestUtil.uploadNewFileVersion(client, file, profileManager, fileManager, limitingConfig);

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// verify that only one version is online
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);
		MetaFile metaDocument = (MetaFile) ProcessTestUtil.getMetaDocument(client, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@Test
	public void testCleanupMaxSize() throws IOException, GetFailedException {
		// overwrite config and set the currently max limit
		final long fileSize = file.length();
		IFileConfiguration limitingConfig = new IFileConfiguration() {

			@Override
			public int getMaxSizeAllVersions() {
				return (int) fileSize;
			}

			@Override
			public int getMaxNumOfVersions() {
				return Integer.MAX_VALUE;
			}

			@Override
			public int getMaxFileSize() {
				return Integer.MAX_VALUE;
			}

			@Override
			public int getChunkSize() {
				return H2HConstants.DEFAULT_CHUNK_SIZE;
			}
		};

		// update the file (append some data)
		FileUtils.write(file, NetworkTestUtil.randomString(), true);
		FileUtils.write(file, NetworkTestUtil.randomString(), true);

		NetworkManager client = network.get(1);
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		ProcessTestUtil.uploadNewFileVersion(client, file, profileManager, fileManager, limitingConfig);

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// verify that only one version is online
		UserProfile userProfile = profileManager.getUserProfile(-1, false);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManager);
		MetaFile metaDocument = (MetaFile) ProcessTestUtil.getMetaDocument(client, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManager.getRoot().toFile());
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
