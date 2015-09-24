package org.hive2hive.core.processes.share;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileEventListener;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;

public abstract class BaseShareReadWriteTest extends H2HJUnitTest {

	protected static final int MAX_NUM_CHUNKS = 2;

	protected static List<NetworkManager> network;
	protected static NetworkManager nodeA;
	protected static NetworkManager nodeB;

	protected static File rootA;
	protected static File rootB;
	protected File sharedFolderA;
	protected File sharedFolderB;

	protected static UserCredentials userA;
	protected static UserCredentials userB;

	protected static TestFileEventListener eventsAtA;
	protected static TestFileEventListener eventsAtB;

	protected static void setupNetwork() throws NoPeerConnectionException, IOException {
		logger.info("Setup network.");
		network = NetworkTestUtil.createNetwork(DEFAULT_NETWORK_SIZE);

		logger.info("Create user A.");
		rootA = tempFolder.newFolder();
		userA = generateRandomCredentials();
		nodeA = network.get(0);
		logger.info("Register and login user A.");
		UseCaseTestUtil.registerAndLogin(userA, nodeA, rootA);

		eventsAtA = new TestFileEventListener(nodeA);
		nodeA.getEventBus().subscribe(eventsAtA);

		logger.info("Create user B.");
		rootB = tempFolder.newFolder();
		userB = generateRandomCredentials();
		nodeB = network.get(1);
		logger.info("Register and login user B.");
		UseCaseTestUtil.registerAndLogin(userB, nodeB, rootB);

		eventsAtB = new TestFileEventListener(nodeB);
		nodeB.getEventBus().subscribe(eventsAtB);
	}

	/**
	 * Setup network. Setup two users with each one client, log them in.
	 * 
	 * @throws Exception
	 */
	protected void setupShares(PermissionType permissionB) throws Exception {
		String folderName = "sharedFolder_" + randomString();
		logger.info("Upload folder '{}' from A.", folderName);
		sharedFolderA = new File(rootA, folderName);
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(nodeA, sharedFolderA);

		logger.info("Share folder '{}' with user B giving permissions: {}.", folderName, permissionB);
		UseCaseTestUtil.shareFolder(nodeA, sharedFolderA, userB.getUserId(), permissionB);
		sharedFolderB = new File(rootB, folderName);
		waitTillSynchronized(sharedFolderB, true);
	}

	@AfterClass
	public static void afterTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
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
		}
	}

}
