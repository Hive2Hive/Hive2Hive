package org.hive2hive.core.processes.share;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileEventListener;
import org.hive2hive.core.utils.UseCaseTestUtil;
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
	private static final int networkSize = 6;
	private static ArrayList<NetworkManager> network;

	private static File rootA;
	private static File rootB;
	private static UserCredentials userA;
	private static UserCredentials userB;
	private static TestFileEventListener eventB;

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
		userA = generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		rootB = FileTestUtil.getTempDirectory();
		userB = generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);

		eventB = new TestFileEventListener();
		network.get(1).getEventBus().subscribe(eventB);
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

		// check the events at user B
		File sharedFolderAtB = new File(rootB, folderToShare.getName());
		IFileShareEvent shared = waitTillShared(sharedFolderAtB);
		Assert.assertEquals(userA.getUserId(), shared.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, shared.getUserPermission().getPermission());

		File file1AtB = new File(sharedFolderAtB, file1.getName());
		IFileAddEvent added = waitTillAdded(file1AtB);
		Assert.assertTrue(added.isFile());

		File file2AtB = new File(sharedFolderAtB, file2.getName());
		added = waitTillAdded(file2AtB);
		Assert.assertTrue(added.isFile());

		File subfolderAtB = new File(sharedFolderAtB, subfolder.getName());
		added = waitTillAdded(subfolderAtB);
		Assert.assertTrue(added.isFolder());

		File file3AtB = new File(subfolderAtB, file3.getName());
		added = waitTillAdded(file3AtB);
		Assert.assertTrue(added.isFile());
	}

	@Test
	public void shareEmptyFolder() throws IOException, IllegalFileLocation, NoSessionException, GetFailedException,
			InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File sharedFolderAtA = new File(rootA, randomString());
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		// share the empty folder
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId(), PermissionType.WRITE);

		// wait for userB to process the user profile task
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		IFileShareEvent shared = waitTillShared(sharedFolderAtB);
		Assert.assertEquals(userA.getUserId(), shared.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, shared.getUserPermission().getPermission());
	}

	private static IFileShareEvent waitTillShared(File sharedFolder) {
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (eventB.getShared(sharedFolder) == null);
		return eventB.getShared(sharedFolder);
	}

	private static IFileAddEvent waitTillAdded(File addedFile) {
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (eventB.getAdded(addedFile) == null);
		return eventB.getAdded(addedFile);
	}

	@AfterClass
	public static void endTest() throws IOException {
		FileUtils.deleteDirectory(rootA);
		FileUtils.deleteDirectory(rootB);
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
