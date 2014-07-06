package org.hive2hive.core.processes.files;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests downloading a file.
 * 
 * @author Nico, Seppi
 */
public class DownloadLargeFileTest extends H2HJUnitTest {

	private final static int networkSize = 2;
	private final static int CHUNK_SIZE = 1024;

	private static List<NetworkManager> network;
	private static NetworkManager uploader;
	private static NetworkManager downloader;
	private static UserCredentials userCredentials;
	private static File uploaderRoot;
	private static File downloaderRoot;

	private static File uploadedFile;
	private static String testContent;
	private static Index fileNode;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DownloadLargeFileTest.class;
		beforeClass();
		// setup a network
		network = NetworkTestUtil.createNetwork(networkSize);
		uploader = network.get(0);
		downloader = network.get(1);

		// create user, register and login both clients
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		uploaderRoot = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.registerAndLogin(userCredentials, uploader, uploaderRoot);

		// upload the large file before the 2nd peer logs in
		uploadLargeFile();

		downloaderRoot = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		UseCaseTestUtil.login(userCredentials, downloader, downloaderRoot);
	}

	public static void uploadLargeFile() throws IOException, NoSessionException, NoPeerConnectionException,
			GetFailedException {
		// upload a large file
		BigInteger maxFileSize = uploader.getSession().getFileConfiguration().getMaxFileSize();
		int minChunks = (int) maxFileSize.longValue() / CHUNK_SIZE;
		String fileName = NetworkTestUtil.randomString();
		uploadedFile = FileTestUtil.createFileRandomContent(fileName, minChunks + 1, uploaderRoot, CHUNK_SIZE);
		testContent = FileUtils.readFileToString(uploadedFile);
		UseCaseTestUtil.uploadNewFile(uploader, uploadedFile);
		UserProfile up = UseCaseTestUtil.getUserProfile(network.get(0), userCredentials);
		fileNode = up.getRoot().getChildByName(fileName);
	}

	@Test
	public void testDownloadLargeFile() throws IOException, NoSessionException, GetFailedException,
			NoPeerConnectionException {
		// download large file
		UseCaseTestUtil.downloadFile(downloader, fileNode.getFilePublicKey());

		// the downloaded file should now be on the disk
		File downloadedFile = new File(downloaderRoot, fileNode.getName());
		Assert.assertTrue(downloadedFile.exists());

		String content = FileUtils.readFileToString(downloadedFile);
		Assert.assertEquals(testContent, content);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(uploaderRoot);
		FileUtils.deleteDirectory(downloaderRoot);
		afterClass();
	}
}
