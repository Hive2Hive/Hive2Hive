package org.hive2hive.core.test.process.share;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.share.ShareFolderProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
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

public class ShareFolderTest extends H2HJUnitTest {

	private final int networkSize = 3;
	private List<NetworkManager> network;
	private IFileConfiguration config = new TestFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ShareFolderTest.class;
		beforeClass();
	}

	@Before
	public void connect() {
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void shareFolderTest() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException {
		File rootA = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManagerA = new FileManager(rootA.toPath());

		UserCredentials userA = NetworkTestUtil.generateRandomCredentials();
		UserProfileManager profileManagerA = new UserProfileManager(network.get(0), userA);
		ProcessTestUtil.register(userA, network.get(0));
		ProcessTestUtil.login(userA, network.get(0), rootA);

		File rootB = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileManager fileManagerB = new FileManager(rootB.toPath());

		UserCredentials userB = NetworkTestUtil.generateRandomCredentials();
		ProcessTestUtil.register(userB, network.get(1));

		File folderToShare = new File(rootA, "folder1");
		folderToShare.mkdirs();

		ProcessTestUtil.uploadNewFile(network.get(0), folderToShare, profileManagerA, fileManagerA, config);

		ShareFolderProcess shareFolderProcess = new ShareFolderProcess(folderToShare, userB.getUserId(),
				network.get(0));
		TestProcessListener listener = new TestProcessListener();
		shareFolderProcess.addListener(listener);
		shareFolderProcess.start();

		ProcessTestUtil.waitTillSucceded(listener, 10);

		FileUtils.deleteDirectory(fileManagerA.getRoot().toFile());
		FileUtils.deleteDirectory(fileManagerB.getRoot().toFile());
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
