package org.hive2hive.core.processes.share.read;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#READ} permission. Tests moving files and folders out of a
 * shared folder.
 * 
 * @author Seppi
 */
public class SharedFolderWithReadPermissionMoveOutTest extends H2HJUnitTest {

	private static final int CHUNK_SIZE = 1024;
	private static final int maxNumChunks = 2;

	private static List<NetworkManager> network;
	private static NetworkManager nodeA;
	private static NetworkManager nodeB;

	private static File rootA;
	private static File rootB;
	private static File sharedFolderA;
	private static File sharedFolderB;
	private static File subFolderA;
	private static File subFolderB;

	private static UserCredentials userA;
	private static UserCredentials userB;

	/**
	 * Setup network. Setup two users with each one client, log them in.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SharedFolderWithReadPermissionMoveOutTest.class;
		beforeClass();

		logger.info("Setup network.");
		network = NetworkTestUtil.createNetwork(3);
		nodeA = network.get(0);
		nodeB = network.get(1);

		logger.info("Create user A.");
		rootA = FileTestUtil.getTempDirectory();
		userA = NetworkTestUtil.generateRandomCredentials();
		logger.info("Register and login user A.");
		UseCaseTestUtil.registerAndLogin(userA, nodeA, rootA);

		logger.info("Create user B.");
		rootB = FileTestUtil.getTempDirectory();
		userB = NetworkTestUtil.generateRandomCredentials();
		logger.info("Register and login user B.");
		UseCaseTestUtil.registerAndLogin(userB, nodeB, rootB);

		sharedFolderA = new File(rootA, "sharedfolder");
		sharedFolderA.mkdirs();
		logger.info("Upload folder '{}' from A.", sharedFolderA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, sharedFolderA);

		logger.info("Share folder '{}' with user B giving write permission.", sharedFolderA.getName());
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.READ);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB, true);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", rootA.toPath().relativize(subFolderA.toPath()));
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderA, CHUNK_SIZE);
		Path relativePathOld = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' at A.", relativePathOld.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePathOld.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file '{}' at A to root folder of A.", relativePathOld.toString());
		File movedFileFromAAtA = new File(rootA, fileFromAAtA.getName());
		Path relativePathNew = rootA.toPath().relativize(movedFileFromAAtA.toPath());
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till moving of file '{}' to '{}' gets synchronized with B.", relativePathOld.toString(),
				relativePathNew.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndexesAfterMoving(relativePathOld, relativePathNew);
	}

	@Test
	public void testSynchronizeAddFileAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' at A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Try to move file '{}' at B to root folder of B.", relativePath.toString());
		File movingFileAtB = new File(rootB, fileFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingMoving(relativePath);
	}

	@Test
	public void testSynchronizeAddSubFileAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(maxNumChunks) + 1,
				subFolderA, CHUNK_SIZE);
		Path relativePathOld = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' at A.", relativePathOld.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePathOld.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file '{}' at A to root folder of A.", relativePathOld.toString());
		File movedFileFromAAtA = new File(rootA, fileFromAAtA.getName());
		Path relativePathNew = rootA.toPath().relativize(movedFileFromAAtA.toPath());
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till moving of file '{}' to '{}' gets synchronized with B.", relativePathOld.toString(),
				relativePathNew.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndexesAfterMoving(relativePathOld, relativePathNew);
	}

	@Test
	public void testSynchronizeAddSubfileAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(maxNumChunks) + 1,
				subFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' at A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Try to move file '{}' at B to root folder of B.", relativePath.toString());
		File movingFileAtB = new File(rootB, fileFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingMoving(relativePath);
	}

	@Test
	public void testSynchronizeAddFolderAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder1FromA");
		folderFromAAtA.mkdir();
		Path relativePathOld = rootA.toPath().relativize(folderFromAAtA.toPath());
		logger.info("Upload a new folder '{}' at A.", relativePathOld.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", relativePathOld.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder '{}' at A to root folder of A.", relativePathOld.toString());
		File movedFolderFromAAtA = new File(rootA, folderFromAAtA.getName());
		Path relativePathNew = rootA.toPath().relativize(movedFolderFromAAtA.toPath());
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moving of folder '{}' to '{}' gets synchronized with B.", relativePathOld.toString(),
				relativePathNew.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndexesAfterMoving(relativePathOld, relativePathNew);
	}

	@Test
	public void testSynchronizeAddFolderAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder2FromA");
		folderFromAAtA.mkdir();
		Path relativePath = rootA.toPath().relativize(folderFromAAtA.toPath());
		logger.info("Upload a new folder '{}' at A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", relativePath.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Try to move folder '{}' at B to root folder of B.", relativePath.toString());
		File movingFolderAtB = new File(rootB, folderFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingMoving(relativePath);
	}

	@Test
	public void testSynchronizeAddSubfolderAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder1FromA");
		folderFromAAtA.mkdir();
		Path relativePathOld = rootA.toPath().relativize(folderFromAAtA.toPath());
		logger.info("Upload a new folder '{}' at A.", relativePathOld.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", relativePathOld.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder '{}' at A to root folder of A.", relativePathOld.toString());
		File movedFolderFromAAtA = new File(rootA, folderFromAAtA.getName());
		Path relativePathNew = rootA.toPath().relativize(movedFolderFromAAtA.toPath());
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moving of folder '{}' to '{}' gets synchronized with B.", relativePathOld.toString(),
				relativePathNew.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndexesAfterMoving(relativePathOld, relativePathNew);
	}

	@Test
	public void testSynchronizeAddSubfolderAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder2FromA");
		folderFromAAtA.mkdir();
		Path relativePath = rootA.toPath().relativize(folderFromAAtA.toPath());
		logger.info("Upload a new folder '{}' at A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", relativePath.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Try to move folder '{}' at B to root folder of B.", relativePath.toString());
		File movingFolderAtB = new File(rootB, folderFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingMoving(relativePath);
	}

	/**
	 * Waits a certain amount of time till a file appears (add) or disappears (delete).
	 * 
	 * @param synchronizingFile
	 *            the file to synchronize
	 * @param appearing
	 *            <code>true</code> if file should appear, <code>false</code> if file should disappear
	 */
	private static void waitTillSynchronized(File synchronizingFile, boolean appearing) {
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

	private static void checkIndexesAfterMoving(Path oldPath, Path newPath) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index indexOldA = userProfileA.getFileByPath(oldPath);
		// should have been deleted
		Assert.assertNull(indexOldA);

		UserProfile userProfileB = nodeB.getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index indexOldB = userProfileB.getFileByPath(oldPath);
		// should have been deleted
		Assert.assertNull(indexOldB);

		Index indexNew = userProfileA.getFileByPath(newPath);
		// should have been created
		Assert.assertNotNull(indexNew);
		// check isShared flag
		Assert.assertFalse(indexNew.isShared());
		// check if content protection keys are the default content protection key
		Assert.assertTrue(indexNew.getProtectionKeys().getPrivate().equals(userProfileA.getProtectionKeys().getPrivate()));
		Assert.assertTrue(indexNew.getProtectionKeys().getPublic().equals(userProfileA.getProtectionKeys().getPublic()));
		// check write access
		Assert.assertTrue(indexNew.canWrite());
		// check user permissions
		Set<String> users = indexNew.getCalculatedUserList();
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(userProfileA.getUserId()));
		// check user permissions in case of a folder
		if (indexNew.isFolder()) {
			Set<UserPermission> permissions = ((FolderIndex) indexNew).getCalculatedUserPermissions();
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userProfileA.getUserId(), PermissionType.WRITE)));
		}
	}

	private static void checkIndexAfterTryingMoving(Path relativePath) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index indexA = userProfileA.getFileByPath(relativePath);

		UserProfile userProfileB = nodeB.getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index indexB = userProfileB.getFileByPath(relativePath);

		// check if userA's content protection keys are other ones
		Assert.assertFalse(indexA.getProtectionKeys().getPrivate().equals(userProfileA.getProtectionKeys().getPrivate()));
		Assert.assertFalse(indexA.getProtectionKeys().getPublic().equals(userProfileA.getProtectionKeys().getPublic()));
		// check if user B has no content protection keys
		Assert.assertNull(indexB.getProtectionKeys());

		// check if isShared flag is set
		Assert.assertTrue(indexA.isShared());
		Assert.assertTrue(indexB.isShared());

		// check write access
		Assert.assertTrue(indexA.canWrite());
		// user B has no write access
		Assert.assertFalse(indexB.canWrite());

		// check user permissions at A
		Set<String> usersA = indexA.getCalculatedUserList();
		Assert.assertEquals(2, usersA.size());
		Assert.assertTrue(usersA.contains(userA.getUserId()));
		Assert.assertTrue(usersA.contains(userB.getUserId()));

		// check user permissions at A
		Set<String> usersB = indexB.getCalculatedUserList();
		Assert.assertEquals(2, usersB.size());
		Assert.assertTrue(usersB.contains(userA.getUserId()));
		Assert.assertTrue(usersB.contains(userB.getUserId()));

		// check user permissions in case of a folder at A
		if (indexA.isFolder()) {
			Assert.assertTrue(indexA.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) indexA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
		}

		// check user permissions in case of a folder at B
		if (indexB.isFolder()) {
			Assert.assertTrue(indexB.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) indexB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
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
