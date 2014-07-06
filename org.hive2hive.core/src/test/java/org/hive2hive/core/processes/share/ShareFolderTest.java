package org.hive2hive.core.processes.share;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the share function. A folder can be shared among multiple users.
 * 
 * @author Nico, Seppi
 */
public class ShareFolderTest extends H2HJUnitTest {

	private final static int CHUNK_SIZE = 1024;
	private static final int networkSize = 3;
	private static List<NetworkManager> network;

	private static File rootA;
	private static File rootB;
	private static UserCredentials userA;
	private static UserCredentials userB;

	/**
	 * Setup two users with each one client, log them in
	 * 
	 * @throws NoPeerConnectionException
	 */
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ShareFolderTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
		rootA = FileTestUtil.getTempDirectory();
		userA = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		rootB = FileTestUtil.getTempDirectory();
		userB = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);
	}

	@Test
	public void shareFilledFolderTest() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File folderToShare = new File(rootA, "sharedFolder");
		folderToShare.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderToShare);

		File file1 = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(5) + 1, folderToShare, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1);
		File file2 = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5) + 1, folderToShare, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file2);
		File subfolder = new File(folderToShare, "subfolder1");
		subfolder.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subfolder);
		File file3 = FileTestUtil.createFileRandomContent("file3", new Random().nextInt(5) + 1, subfolder, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file3);

		// share the filled folder
		UseCaseTestUtil.shareFolder(network.get(0), folderToShare, userB.getUserId(), PermissionType.WRITE);

		// check the files and the folders at user B
		File sharedFolderAtB = new File(rootB, folderToShare.getName());
		waitTillSynchronized(sharedFolderAtB, true);

		File file1AtB = new File(sharedFolderAtB, file1.getName());
		waitTillSynchronized(file1AtB, true);
		Assert.assertEquals(file1.length(), file1AtB.length());

		File file2AtB = new File(sharedFolderAtB, file2.getName());
		waitTillSynchronized(file2AtB, true);
		Assert.assertEquals(file2.length(), file2AtB.length());

		File subfolderAtB = new File(sharedFolderAtB, subfolder.getName());
		waitTillSynchronized(subfolderAtB, true);

		File file3AtB = new File(subfolderAtB, file3.getName());
		waitTillSynchronized(file3AtB, true);
		Assert.assertEquals(file3.length(), file3AtB.length());
	}

	@Test
	public void shareEmptyFolder() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File sharedFolderAtA = new File(rootA, NetworkTestUtil.randomString());
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		// share the empty folder
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId(), PermissionType.WRITE);

		// wait for userB to process the user profile task
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		waitTillSynchronized(sharedFolderAtB, true);
		Assert.assertTrue(sharedFolderAtB.isDirectory());
	}

	private static void waitTillSynchronized(File synchronizingFile, boolean appearing) {
		H2HWaiter waiter = new H2HWaiter(1000);
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

	@AfterClass
	public static void endTest() throws IOException {
		FileUtils.deleteDirectory(rootA);
		FileUtils.deleteDirectory(rootB);
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
