package org.hive2hive.core.processes.files.update;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestProcessComponentListener;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.hive2hive.core.utils.helper.DenyingMessageReplyHandler;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests uploading a new version of a file.
 * 
 * @author Nico, Seppi
 */
public class UpdateFileTest extends H2HJUnitTest {

	private final static int networkSize = 6;

	private static ArrayList<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static File uploaderRoot;
	private static File file;
	private static NetworkManager uploader;
	private static NetworkManager downloader;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UpdateFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		NetworkManager registrer = network.get(0);
		uploader = network.get(1);
		downloader = network.get(2);
		userCredentials = generateRandomCredentials();

		// make the two clients ignore each other
		uploader.getConnection().getPeer().peer().objectDataReply(new DenyingMessageReplyHandler());
		downloader.getConnection().getPeer().peer().objectDataReply(new DenyingMessageReplyHandler());

		// create the roots and the file manager
		uploaderRoot = FileTestUtil.getTempDirectory();
		File rootDownloader = FileTestUtil.getTempDirectory();

		// register a user
		UseCaseTestUtil.register(userCredentials, registrer);
		UseCaseTestUtil.login(userCredentials, uploader, uploaderRoot);
		UseCaseTestUtil.login(userCredentials, downloader, rootDownloader);

		// create a file
		file = FileTestUtil.createFileRandomContent(3, uploaderRoot, H2HConstants.DEFAULT_CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(uploader, file);
	}

	@Test
	public void testUploadNewVersion() throws IOException, GetFailedException, NoSessionException, NoPeerConnectionException {
		// overwrite the content in the file
		String newContent = randomString();
		FileUtils.write(file, newContent, false);
		byte[] md5UpdatedFile = HashUtil.hash(file);

		// upload the new version
		UseCaseTestUtil.uploadNewVersion(uploader, file);

		// download the file and check if version is newer
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		Index index = userProfile.getFileByPath(file, uploaderRoot);
		File downloaded = UseCaseTestUtil.downloadFile(downloader, index.getFilePublicKey());

		// new content should be latest one
		Assert.assertEquals(newContent, FileUtils.readFileToString(downloaded));

		// check the md5 hash
		Assert.assertTrue(HashUtil.compare(downloaded, md5UpdatedFile));
	}

	@Test
	public void testUploadSameVersion() throws IllegalArgumentException, GetFailedException, IOException, NoSessionException,
			InvalidProcessStateException, IllegalArgumentException, NoPeerConnectionException {
		// upload the same content again
		IProcessComponent<Void> process = ProcessFactory.instance().createUpdateFileProcess(file, uploader,
				FileConfiguration.createDefault());
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);

		try {
			process.execute();
		} catch (ProcessExecutionException ex) {
			// the below waiter waits for fail
		}

		H2HWaiter waiter = new H2HWaiter(60);
		do {
			waiter.tickASecond();
		} while (!listener.hasExecutionFailed());

		// verify if the md5 hash did not change
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		FileIndex fileNode = (FileIndex) userProfile.getFileByPath(file, uploaderRoot);
		Assert.assertTrue(HashUtil.compare(file, fileNode.getMD5()));
	}

	@Test
	public void testCleanupMaxNumVersions() throws IOException, GetFailedException, NoSessionException,
			IllegalArgumentException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		// overwrite config
		IFileConfiguration limitingConfig = new IFileConfiguration() {

			@Override
			public BigInteger getMaxSizeAllVersions() {
				return BigInteger.valueOf(Long.MAX_VALUE);
			}

			@Override
			public int getMaxNumOfVersions() {
				return 1;
			}

			@Override
			public BigInteger getMaxFileSize() {
				return BigInteger.valueOf(Long.MAX_VALUE);
			}

			@Override
			public int getChunkSize() {
				return H2HConstants.DEFAULT_CHUNK_SIZE;
			}
		};

		H2HSession session = uploader.getSession();
		SessionParameters params = new SessionParameters(session.getFileAgent());
		params.setDownloadManager(session.getDownloadManager());
		params.setKeyManager(session.getKeyManager());
		params.setLocationsManager(session.getLocationsManager());
		params.setUserProfileManager(session.getProfileManager());
		H2HSession newSession = new H2HSession(params);
		uploader.setSession(newSession);

		// update the file
		FileUtils.write(file, "bla", false);
		UseCaseTestUtil.uploadNewVersion(uploader, file, limitingConfig);

		// verify that only one version is online
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		Index fileNode = userProfile.getFileByPath(file, uploaderRoot);
		MetaFileSmall metaFileSmall = (MetaFileSmall) UseCaseTestUtil.getMetaFile(downloader, fileNode.getFileKeys());
		Assert.assertEquals(1, metaFileSmall.getVersions().size());
	}

	@Test
	public void testCleanupMaxSize() throws IOException, GetFailedException, NoSessionException, IllegalArgumentException,
			NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		// overwrite config and set the currently max limit
		final long fileSize = file.length();
		IFileConfiguration limitingConfig = new IFileConfiguration() {

			@Override
			public BigInteger getMaxSizeAllVersions() {
				return BigInteger.valueOf(fileSize);
			}

			@Override
			public int getMaxNumOfVersions() {
				return Integer.MAX_VALUE;
			}

			@Override
			public BigInteger getMaxFileSize() {
				return BigInteger.valueOf(Long.MAX_VALUE);
			}

			@Override
			public int getChunkSize() {
				return H2HConstants.DEFAULT_CHUNK_SIZE;
			}
		};

		H2HSession session = uploader.getSession();
		SessionParameters params = new SessionParameters(session.getFileAgent());
		params.setDownloadManager(session.getDownloadManager());
		params.setKeyManager(session.getKeyManager());
		params.setLocationsManager(session.getLocationsManager());
		params.setUserProfileManager(session.getProfileManager());
		H2HSession newSession = new H2HSession(params);
		uploader.setSession(newSession);

		// update the file (append some data)
		FileUtils.write(file, randomString(), true);

		UseCaseTestUtil.uploadNewVersion(uploader, file, limitingConfig);

		// verify that only one version is online
		UserProfile userProfile = UseCaseTestUtil.getUserProfile(downloader, userCredentials);
		Index fileNode = userProfile.getFileByPath(file, uploaderRoot);
		MetaFileSmall metaFileSmall = (MetaFileSmall) UseCaseTestUtil.getMetaFile(downloader, fileNode.getFileKeys());
		Assert.assertEquals(1, metaFileSmall.getVersions().size());
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(uploaderRoot);
		afterClass();
	}
}
