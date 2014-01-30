package org.hive2hive.processes.test.implementations.share;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.share.ShareFolderProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.files.NewFileTest;
import org.hive2hive.processes.test.util.UseCaseTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShareFolderTest extends H2HJUnitTest {

	private static final int networkSize = 3;
	private static final String FOLDER_NAME = "folder1";

	private List<NetworkManager> network;
	private File rootA;
	private File rootB;
	private UserCredentials userA;
	private UserCredentials userB;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NewFileTest.class;
		beforeClass();
	}

	/**
	 * Setup two users with each one client, log them in and add a file at client 1
	 */
	@Before
	public void setup() throws NoSessionException {
		network = NetworkTestUtil.createNetwork(networkSize);

		rootA = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());

		userA = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		rootB = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		userB = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);

		File folderToShare = new File(rootA, FOLDER_NAME);
		folderToShare.mkdirs();

		UseCaseTestUtil.uploadNewFile(network.get(0), folderToShare);
	}

	@Test
	public void shareFolderTest() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		UseCaseTestUtil.shareFolder(network.get(0), new File(rootA, FOLDER_NAME), userB.getUserId());

		File folderAtB = new File(rootB, FOLDER_NAME);
		Assert.assertTrue(folderAtB.exists());
	}

	@Test
	public void initiallizationTest() {
		File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());

		File file = new File(root, NetworkTestUtil.randomString());
		try {
			new ShareFolderProcess(file, "random", null);
		} catch (IllegalArgumentException e) {
			// is ok
		} catch (IllegalFileLocation | NoSessionException e) {
			Assert.fail();
		}

		File folder = new File(root, "folder1");
		folder.mkdirs();
		try {
			new ShareFolderProcess(folder, "random", network.get(0));
		} catch (NoSessionException e) {
			// is ok
		} catch (IllegalFileLocation | IllegalArgumentException e) {
			Assert.fail();
		}

		File otherRoot = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		File otherFolder = new File(otherRoot, NetworkTestUtil.randomString());
		otherFolder.mkdirs();
		try {
			network.get(0).setSession(new H2HSession(null, null, null, new FileManager(root.toPath())));
			new ShareFolderProcess(otherFolder, "random", network.get(0));
		} catch (IllegalFileLocation e) {
			// is ok
		} catch (NoSessionException | IllegalArgumentException e) {
			Assert.fail();
		}

		try {
			new ShareFolderProcess(root, "random", network.get(0));
		} catch (IllegalFileLocation e) {
			// is ok
		} catch (NoSessionException | IllegalArgumentException e) {
			Assert.fail();
		}

		root.delete();
		file.delete();
		folder.delete();
		otherRoot.delete();
		otherFolder.delete();
	}

	@After
	public void shutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}

}
