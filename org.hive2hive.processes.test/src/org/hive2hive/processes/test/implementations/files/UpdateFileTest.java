package org.hive2hive.processes.test.implementations.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
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
 * Tests uploading a new version of a file.
 * 
 * @author Nico
 * 
 */
public class UpdateFileTest extends H2HJUnitTest {

	private final int networkSize = 5;
	private final IFileConfiguration config = new TestFileConfiguration();
	private List<NetworkManager> network;
	private UserCredentials userCredentials;
	private FileManager fileManagerUploader;
	private File file;

	private NetworkManager uploader;
	private NetworkManager downloader;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UpdateFileTest.class;
		beforeClass();

	}

	@Before
	public void createProfileUploadBaseFile() throws IOException, IllegalFileLocation, NoSessionException,
			NoPeerConnectionException {
		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkManager registrar = network.get(0);
		uploader = network.get(1);
		downloader = network.get(2);

		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// create the roots and the filemanagers
		File rootUploader = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManagerUploader = new FileManager(rootUploader.toPath());
		File rootDownloader = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());

		// register a user
		UseCaseTestUtil.register(userCredentials, registrar);
		UseCaseTestUtil.login(userCredentials, uploader, rootUploader);
		UseCaseTestUtil.login(userCredentials, downloader, rootDownloader);

		// create a file
		file = FileTestUtil.createFileRandomContent(3, rootUploader, config);
		UseCaseTestUtil.uploadNewFile(uploader, file);
	}

	@Test
	public void testUploadNewVersion() throws IOException, GetFailedException, NoSessionException,
			NoPeerConnectionException {
		// overwrite the content in the file
		String newContent = NetworkTestUtil.randomString();
		FileUtils.write(file, newContent, false);
		byte[] md5UpdatedFile = EncryptionUtil.generateMD5Hash(file);

		// upload the new version
		UseCaseTestUtil.uploadNewVersion(uploader, file);

		// download the file and check if version is newer
		UseCaseTestUtil.login(userCredentials, downloader, FileUtils.getTempDirectory());
		FileTreeNode fileNode = UseCaseTestUtil.getUserProfile(downloader, userCredentials).getFileByPath(
				file, fileManagerUploader);
		File downloaded = UseCaseTestUtil.downloadFile(downloader, fileNode.getFileKey());

		// new content should be latest one
		Assert.assertEquals(newContent, FileUtils.readFileToString(downloaded));

		// check the md5 hash
		Assert.assertTrue(H2HEncryptionUtil.compareMD5(downloaded, md5UpdatedFile));
	}

	@Test
	public void testUploadSameVersion() throws IllegalFileLocation, GetFailedException, IOException,
			NoSessionException, InvalidProcessStateException, IllegalArgumentException,
			NoPeerConnectionException {
		// upload the same content again
		IProcessComponent process = ProcessFactory.instance().createUpdateFileProcess(file, uploader);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(60);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		// verify if the md5 hash did not change
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManagerUploader);
		Assert.assertTrue(H2HEncryptionUtil.compareMD5(file, fileNode.getMD5()));

		// verify that only one version was created
		MetaFile metaDocument = (MetaFile) UseCaseTestUtil.getMetaDocument(downloader, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewFolderVersion() throws IllegalFileLocation, NoSessionException,
			NoPeerConnectionException {
		// new folder version is illegal
		File folder = new File(fileManagerUploader.getRoot().toFile(), "test-folder");
		folder.mkdir();

		// upload the file
		UseCaseTestUtil.uploadNewFile(uploader, folder);

		// try to upload the same folder again (which is invalid)
		UseCaseTestUtil.uploadNewVersion(uploader, folder);
	}

	@Test
	public void testCleanupMaxNumVersions() throws IOException, GetFailedException, NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
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

		H2HSession session = uploader.getSession();
		H2HSession newSession = new H2HSession(session.getKeyPair(), session.getProfileManager(),
				limitingConfig, session.getFileManager());
		uploader.setSession(newSession);

		// update the file
		FileUtils.write(file, "bla", false);
		UseCaseTestUtil.uploadNewVersion(uploader, file);

		// TODO wait for other processes
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// verify that only one version is online
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManagerUploader);
		MetaFile metaDocument = (MetaFile) UseCaseTestUtil.getMetaDocument(downloader, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@Test
	public void testCleanupMaxSize() throws IOException, GetFailedException, NoSessionException,
			IllegalArgumentException, NoPeerConnectionException {
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

		H2HSession session = uploader.getSession();
		H2HSession newSession = new H2HSession(session.getKeyPair(), session.getProfileManager(),
				limitingConfig, session.getFileManager());
		uploader.setSession(newSession);

		// update the file (append some data)
		FileUtils.write(file, NetworkTestUtil.randomString(), true);
		FileUtils.write(file, NetworkTestUtil.randomString(), true);

		UseCaseTestUtil.uploadNewVersion(uploader, file);
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!ProcessManager.getInstance().getAllProcesses().isEmpty());

		// verify that only one version is online
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		FileTreeNode fileNode = userProfile.getFileByPath(file, fileManagerUploader);
		MetaFile metaDocument = (MetaFile) UseCaseTestUtil.getMetaDocument(downloader, fileNode.getKeyPair());
		Assert.assertEquals(1, metaDocument.getVersions().size());
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManagerUploader.getRoot().toFile());
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
