package org.hive2hive.core.processes.share;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileEventListener;
import org.hive2hive.core.utils.UseCaseTestUtil;
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

	private static List<NetworkManager> network;

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

		network = NetworkTestUtil.createNetwork(Math.max(DEFAULT_NETWORK_SIZE, 3));
		rootA = tempFolder.newFolder();
		userA = generateRandomCredentials("userA");
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		rootB = tempFolder.newFolder();
		userB = generateRandomCredentials("userB");
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);

		eventB = new TestFileEventListener();
		network.get(1).getEventBus().subscribe(eventB);
	}

	@Test
	public void shareFilledFolderTest() throws IOException, IllegalArgumentException, NoSessionException,
			GetFailedException, InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File folderToShare = new File(rootA, "sharedFolder");
		folderToShare.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderToShare);

		File file1 = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(5) + 1, folderToShare);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1);
		File file2 = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5) + 1, folderToShare);
		UseCaseTestUtil.uploadNewFile(network.get(0), file2);
		File subfolder = new File(folderToShare, "subfolder1");
		subfolder.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subfolder);
		File file3 = FileTestUtil.createFileRandomContent("file3", new Random().nextInt(5) + 1, subfolder);
		UseCaseTestUtil.uploadNewFile(network.get(0), file3);

		// share the filled folder
		UseCaseTestUtil.shareFolder(network.get(0), folderToShare, userB.getUserId(), PermissionType.WRITE);

		// check the events at user B
		File sharedFolderAtB = new File(rootB, folderToShare.getName());
		IFileShareEvent shared = waitTillShared(eventB, sharedFolderAtB);
		Assert.assertEquals(userA.getUserId(), shared.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, shared.getUserPermission(userB.getUserId()).getPermission());
		Assert.assertEquals(2, shared.getUserPermissions().size());

		IFileAddEvent added = waitTillAdded(eventB, sharedFolderAtB);
		Assert.assertTrue(added.isFolder());

		File file1AtB = new File(sharedFolderAtB, file1.getName());
		added = waitTillAdded(eventB, file1AtB);
		Assert.assertTrue(added.isFile());

		File file2AtB = new File(sharedFolderAtB, file2.getName());
		added = waitTillAdded(eventB, file2AtB);
		Assert.assertTrue(added.isFile());

		File subfolderAtB = new File(sharedFolderAtB, subfolder.getName());
		added = waitTillAdded(eventB, subfolderAtB);
		Assert.assertTrue(added.isFolder());

		File file3AtB = new File(subfolderAtB, file3.getName());
		added = waitTillAdded(eventB, file3AtB);
		Assert.assertTrue(added.isFile());
	}

	@Test
	public void shareEmptyFolder() throws IOException, IllegalArgumentException, NoSessionException, GetFailedException,
			InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File sharedFolderAtA = new File(rootA, randomString());
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		// share the empty folder
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId(), PermissionType.WRITE);

		// wait for userB to process the user profile task
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		IFileShareEvent shared = waitTillShared(eventB, sharedFolderAtB);
		Assert.assertEquals(userA.getUserId(), shared.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, shared.getUserPermission(userB.getUserId()).getPermission());
		Assert.assertEquals(2, shared.getUserPermissions().size());

		IFileAddEvent added = waitTillAdded(eventB, sharedFolderAtB);
		Assert.assertTrue(added.isFolder());
	}

	@Test
	public void shareThreeUsers() throws IOException, IllegalArgumentException, NoSessionException, GetFailedException,
			InterruptedException, NoPeerConnectionException {
		File rootC = tempFolder.newFolder();
		UserCredentials userC = generateRandomCredentials("userC");
		UseCaseTestUtil.registerAndLogin(userC, network.get(2), rootC);

		TestFileEventListener eventC = new TestFileEventListener();
		network.get(2).getEventBus().subscribe(eventC);

		// upload an empty folder
		File sharedFolderAtA = new File(rootA, randomString());
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		// share the empty folder with B and C
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId(), PermissionType.WRITE);
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userC.getUserId(), PermissionType.WRITE);

		// wait for userB to process the user profile task
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		IFileShareEvent sharedB = waitTillShared(eventB, sharedFolderAtB);
		Assert.assertEquals(userA.getUserId(), sharedB.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, sharedB.getUserPermission(userA.getUserId()).getPermission());
		Assert.assertEquals(PermissionType.WRITE, sharedB.getUserPermission(userB.getUserId()).getPermission());
		Assert.assertEquals(PermissionType.WRITE, sharedB.getUserPermission(userC.getUserId()).getPermission());
		Assert.assertEquals(3, sharedB.getUserPermissions().size());

		// wait for userB to process the user profile task
		File sharedFolderAtC = new File(rootC, sharedFolderAtA.getName());
		IFileShareEvent sharedC = waitTillShared(eventC, sharedFolderAtC);
		Assert.assertEquals(userA.getUserId(), sharedC.getInvitedBy());
		Assert.assertEquals(PermissionType.WRITE, sharedC.getUserPermission(userA.getUserId()).getPermission());
		Assert.assertEquals(PermissionType.WRITE, sharedC.getUserPermission(userB.getUserId()).getPermission());
		Assert.assertEquals(PermissionType.WRITE, sharedC.getUserPermission(userC.getUserId()).getPermission());
		Assert.assertEquals(3, sharedC.getUserPermissions().size());
	}

	private static IFileShareEvent waitTillShared(TestFileEventListener events, File sharedFolder) {
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (events.getShared(sharedFolder) == null);
		return events.getShared(sharedFolder);
	}

	private static IFileAddEvent waitTillAdded(TestFileEventListener events, File addedFile) {
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (events.getAdded(addedFile) == null);
		return events.getAdded(addedFile);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
