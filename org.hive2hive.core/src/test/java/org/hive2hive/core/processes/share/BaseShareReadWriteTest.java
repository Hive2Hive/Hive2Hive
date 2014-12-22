package org.hive2hive.core.processes.share;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileEventListener;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;

public abstract class BaseShareReadWriteTest extends H2HJUnitTest {

	protected static final int NETWORK_SIZE = 6;
	protected static final int MAX_NUM_CHUNKS = 2;

	protected ArrayList<NetworkManager> network;
	protected NetworkManager nodeA;
	protected NetworkManager nodeB;

	protected File rootA;
	protected File rootB;
	protected File sharedFolderA;
	protected File sharedFolderB;

	protected UserCredentials userA;
	protected UserCredentials userB;

	protected TestFileEventListener eventsAtA;
	protected TestFileEventListener eventsAtB;

	/**
	 * Setup network. Setup two users with each one client, log them in.
	 * 
	 * @throws Exception
	 */
	protected void setupNetworkAndShares(PermissionType permissionB) throws Exception {
		logger.info("Setup network.");
		network = NetworkTestUtil.createNetwork(NETWORK_SIZE);

		logger.info("Create user A.");
		rootA = FileTestUtil.getTempDirectory();
		userA = generateRandomCredentials();
		nodeA = network.get(0);
		logger.info("Register and login user A.");
		UseCaseTestUtil.registerAndLogin(userA, nodeA, rootA);

		eventsAtA = new TestFileEventListener(nodeA);
		nodeA.getEventBus().subscribe(eventsAtA);

		logger.info("Create user B.");
		rootB = FileTestUtil.getTempDirectory();
		userB = generateRandomCredentials();
		nodeB = network.get(1);
		logger.info("Register and login user B.");
		UseCaseTestUtil.registerAndLogin(userB, nodeB, rootB);

		eventsAtB = new TestFileEventListener(nodeB);
		nodeB.getEventBus().subscribe(eventsAtB);

		logger.info("Upload folder 'sharedfolder' from A.");
		sharedFolderA = new File(rootA, "sharedfolder");
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(nodeA, sharedFolderA);

		logger.info("Share folder 'sharedfolder' with user B giving permissions: {}.", permissionB);
		UseCaseTestUtil.shareFolder(nodeA, sharedFolderA, userB.getUserId(), permissionB);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB, true);
	}

	@After
	public void endTest() throws IOException {
		FileUtils.deleteDirectory(rootA);
		FileUtils.deleteDirectory(rootB);

		NetworkTestUtil.shutdownNetwork(network);
	}

	@AfterClass
	public static void afterTest() {
		afterClass();
	}

	/**
	 * Waits a certain amount of time till a file appears (add) or disappears (delete).
	 * 
	 * @param synchronizingFile
	 *            the file to synchronize
	 * @param appearing
	 *            <code>true</code> if file should appear, <code>false</code> if file should disappear
	 */
	protected static void waitTillSynchronized(File synchronizingFile, boolean appearing) {
		H2HWaiter waiter = new H2HWaiter(40);
		if (appearing) {
			do {
				waiter.tickASecond();
			} while (!synchronizingFile.exists());
		} else {
			do {
				waiter.tickASecond();
			} while (synchronizingFile.exists());
		}
	}

	protected static void compareFiles(File originalFile, File synchronizedFile) throws IOException {
		Assert.assertEquals(originalFile.getName(), synchronizedFile.getName());
		if (originalFile.isFile() || synchronizedFile.isFile()) {
			Assert.assertTrue(FileUtils.contentEquals(originalFile, synchronizedFile));
			Assert.assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(synchronizedFile));
		}
	}

}
