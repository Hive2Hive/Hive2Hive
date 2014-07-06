package org.hive2hive.core.processes.share.write;

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
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests moving files and folders out of a
 * shared folder.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionMoveOutTest extends H2HJUnitTest {

	private static final int CHUNK_SIZE = 1024;
	private static final int networkSize = 3;
	private static final int maxNumChunks = 2;
	private static List<NetworkManager> network;

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
		testClass = SharedFolderWithWritePermissionMoveOutTest.class;
		beforeClass();

		logger.info("Setup network.");
		network = NetworkTestUtil.createNetwork(networkSize);

		logger.info("Create user A.");
		rootA = FileTestUtil.getTempDirectory();
		userA = NetworkTestUtil.generateRandomCredentials();
		logger.info("Register and login user A.");
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		logger.info("Create user B.");
		rootB = FileTestUtil.getTempDirectory();
		userB = NetworkTestUtil.generateRandomCredentials();
		logger.info("Register and login user B.");
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);

		logger.info("Upload folder 'sharedfolder' from A.");
		sharedFolderA = new File(rootA, "sharedfolder");
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderA);

		logger.info("Share folder 'sharedfolder' with user B giving write permission.");
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.WRITE);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB, true);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder'.");
		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/file1FromA' from A.");
		File file1FromAAtA = FileTestUtil.createFileRandomContent("file1FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1FromAAtA);

		logger.info("Wait till new file 'sharedFolder/file1FromA' gets synchronized with B.");
		File file1FromAAtB = new File(sharedFolderB, file1FromAAtA.getName());
		waitTillSynchronized(file1FromAAtB, true);

		logger.info("Move file 'sharedFolder/file1FromA' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, file1FromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), file1FromAAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of file 'shardFolder/file1FromA' gets synchronized with B.");
		waitTillSynchronized(file1FromAAtB, false);
		checkIndexes(file1FromAAtA, file1FromAAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/file2FromA' from A.");
		File file2FromAAtA = FileTestUtil.createFileRandomContent("file2FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file2FromAAtA);

		logger.info("Wait till new file 'sharedFolder/file2FromA' gets synchronized with B.");
		File file2FromAAtB = new File(sharedFolderB, file2FromAAtA.getName());
		waitTillSynchronized(file2FromAAtB, true);

		logger.info("Move file 'sharedFolder/file2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, file2FromAAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), file2FromAAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of file 'shardFolder/file2FromA' gets synchronized with A.");
		waitTillSynchronized(file2FromAAtA, false);
		checkIndexes(file2FromAAtA, file2FromAAtB, movedFile1FromAAtB, false);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/file1FromB' from B.");
		File file1FromBAtB = FileTestUtil.createFileRandomContent("file1FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), file1FromBAtB);

		logger.info("Wait till new file 'sharedFolder/file1FromB' gets synchronized with A.");
		File file1FromBAtA = new File(sharedFolderA, file1FromBAtB.getName());
		waitTillSynchronized(file1FromBAtA, true);

		logger.info("Move file 'sharedFolder/file1FromB' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, file1FromBAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), file1FromBAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of file 'shardFolder/file1FromB' gets synchronized with B.");
		waitTillSynchronized(file1FromBAtB, false);
		checkIndexes(file1FromBAtA, file1FromBAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/file2FromB' from B.");
		File file2FromBAtB = FileTestUtil.createFileRandomContent("file2FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2FromBAtB);

		logger.info("Wait till new file 'sharedFolder/file2FromB' gets synchronized with A.");
		File file2FromBAtA = new File(sharedFolderA, file2FromBAtB.getName());
		waitTillSynchronized(file2FromBAtA, true);

		logger.info("Move file 'sharedFolder/file2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, file2FromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), file2FromBAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of file 'shardFolder/file2FromA' gets synchronized with A.");
		waitTillSynchronized(file2FromBAtA, false);
		checkIndexes(file2FromBAtA, file2FromBAtB, movedFile1FromAAtB, false);
	}

	@Test
	public void testSynchronizeAddSubFileFromAMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfile 'sharedFolder/subfolder/subfile1FromA' from A.");
		File subFile1FromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA",
				new Random().nextInt(maxNumChunks) + 1, subFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), subFile1FromAAtA);

		logger.info("Wait till new subfile 'sharedFolder/subfolder/subfile1FromA' gets synchronized with B.");
		File subFile1FromAAtB = new File(subFolderB, subFile1FromAAtA.getName());
		waitTillSynchronized(subFile1FromAAtB, true);

		logger.info("Move subfile 'sharedFolder/subfolder/subfile1FromA' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, subFile1FromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), subFile1FromAAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of subfile 'shardFolder/subfolder/subfile1FromA' gets synchronized with B.");
		waitTillSynchronized(subFile1FromAAtB, false);
		checkIndexes(subFile1FromAAtA, subFile1FromAAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromAMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfile 'sharedFolder/subfolder/subfile2FromA' from A.");
		File subfile2FromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA",
				new Random().nextInt(maxNumChunks) + 1, subFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), subfile2FromAAtA);

		logger.info("Wait till new subfile 'sharedFolder/subfolder/subfile2FromA' gets synchronized with B.");
		File subfile2FromAAtB = new File(subFolderB, subfile2FromAAtA.getName());
		waitTillSynchronized(subfile2FromAAtB, true);

		logger.info("Move subfile 'sharedFolder/subfolder/subfile2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, subfile2FromAAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), subfile2FromAAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of subfile 'shardFolder/subfolder/subfile2FromA' gets synchronized with A.");
		waitTillSynchronized(subfile2FromAAtA, false);
		checkIndexes(subfile2FromAAtA, subfile2FromAAtB, movedFile1FromAAtB, false);
	}

	@Test
	public void testSynchronizeAddSubFileFromBMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/subfolder/subfile1FromB' from B.");
		File subfile1FromBAtB = FileTestUtil.createFileRandomContent("subfile1FromB",
				new Random().nextInt(maxNumChunks) + 1, subFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), subfile1FromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder/subfile1FromB' gets synchronized with A.");
		File subfile1FromBAtA = new File(subFolderA, subfile1FromBAtB.getName());
		waitTillSynchronized(subfile1FromBAtA, true);

		logger.info("Move file 'sharedFolder/subfolder/subfile1FromB' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, subfile1FromBAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), subfile1FromBAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of file 'shardFolder/subfolder/subfile1FromB' gets synchronized with B.");
		waitTillSynchronized(subfile1FromBAtB, false);
		checkIndexes(subfile1FromBAtA, subfile1FromBAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromBMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/subfolder/subfile2FromB' from B.");
		File subfile2FromBAtB = FileTestUtil.createFileRandomContent("subfile2FromB",
				new Random().nextInt(maxNumChunks) + 1, subFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), subfile2FromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder/subfile2FromB' gets synchronized with A.");
		File subfile2FromBAtA = new File(subFolderA, subfile2FromBAtB.getName());
		waitTillSynchronized(subfile2FromBAtA, true);

		logger.info("Move file 'sharedFolder/subfolder/subfile2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, subfile2FromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), subfile2FromBAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of file 'shardFolder/subfolder/subfile2FromA' gets synchronized with A.");
		waitTillSynchronized(subfile2FromBAtA, false);
		checkIndexes(subfile2FromBAtA, subfile2FromBAtB, movedFile1FromAAtB, false);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/file1FromA' from A.");
		File subFolder1FromAAtA = new File(sharedFolderA, "subfolder1FromA");
		subFolder1FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1FromAAtA);

		logger.info("Wait till new file 'sharedFolder/subFolder1FromA' gets synchronized with B.");
		File subFolderFromAAtB = new File(sharedFolderB, subFolder1FromAAtA.getName());
		waitTillSynchronized(subFolderFromAAtB, true);

		logger.info("Move file 'sharedFolder/subFolder1FromA' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, subFolder1FromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), subFolder1FromAAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of file 'shardFolder/subFolder1FromA' gets synchronized with B.");
		waitTillSynchronized(subFolderFromAAtB, false);
		checkIndexes(subFolder1FromAAtA, subFolderFromAAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromAMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/subfolder2FromA' from A.");
		File subFolder2FromAAtA = new File(sharedFolderA, "subfolder2FromA");
		subFolder2FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder2FromAAtA);

		logger.info("Wait till new file 'sharedFolder/subfolder2FromA' gets synchronized with B.");
		File subFolder2FromAAtB = new File(sharedFolderB, subFolder2FromAAtA.getName());
		waitTillSynchronized(subFolder2FromAAtB, true);

		logger.info("Move file 'sharedFolder/subfolder2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, subFolder2FromAAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), subFolder2FromAAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of file 'shardFolder/subfolder2FromA' gets synchronized with A.");
		waitTillSynchronized(subFolder2FromAAtA, false);
		checkIndexes(subFolder2FromAAtA, subFolder2FromAAtB, movedFile1FromAAtB, false);
	}

	@Test
	public void testSynchronizeAddSubFolderFromBMoveOutFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/subfolder1FromB' from B.");
		File subFolder1FromBAtB = new File(sharedFolderB, "subfolder1FromB");
		subFolder1FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subFolder1FromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder1FromB' gets synchronized with A.");
		File subfolder1FromAAtA = new File(sharedFolderA, subFolder1FromBAtB.getName());
		waitTillSynchronized(subfolder1FromAAtA, true);

		logger.info("Move file 'sharedFolder/subfolder1FromB' from A to root folder of A.");
		File movedFile1FromAAtA = new File(rootA, subfolder1FromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), subfolder1FromAAtA, movedFile1FromAAtA);

		logger.info("Wait till moving of file 'shardFolder/subfolder1FromB' gets synchronized with B.");
		waitTillSynchronized(subFolder1FromBAtB, false);
		checkIndexes(subfolder1FromAAtA, subFolder1FromBAtB, movedFile1FromAAtA, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromBMoveOutFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedFolder/subfolder2FromBAtB' from B.");
		File subfolder2FromBAtB = new File(sharedFolderB, "subfolder2FromB");
		subfolder2FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subfolder2FromBAtB);

		logger.info("Wait till new file 'sharedFolder/file2FromB' gets synchronized with A.");
		File subFolder2FromAAtA = new File(sharedFolderA, subfolder2FromBAtB.getName());
		waitTillSynchronized(subFolder2FromAAtA, true);

		logger.info("Move file 'sharedFolder/file2FromA' from B to root folder of B.");
		File movedFile1FromAAtB = new File(rootB, subfolder2FromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), subfolder2FromBAtB, movedFile1FromAAtB);

		logger.info("Wait till moving of file 'shardFolder/file2FromA' gets synchronized with A.");
		waitTillSynchronized(subFolder2FromAAtA, false);
		checkIndexes(subFolder2FromAAtA, subfolder2FromBAtB, movedFile1FromAAtB, false);
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

	private static void checkIndexes(File fileAtA, File fileAtB, File movedFile, boolean userA)
			throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathA = rootA.toPath().relativize(fileAtA.toPath());
		Index indexA = userProfileA.getFileByPath(relativePathA);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathB = rootB.toPath().relativize(fileAtB.toPath());
		Index indexB = userProfileB.getFileByPath(relativePathB);

		// should have been deleted
		Assert.assertNull(indexA);
		Assert.assertNull(indexB);

		if (userA) {
			checkMovedIndex(movedFile, rootA, userProfileA);
		} else {
			checkMovedIndex(movedFile, rootB, userProfileB);
		}

	}

	private static void checkMovedIndex(File movedFile, File root, UserProfile userProfile)
			throws GetFailedException, NoSessionException {
		Path relativePath = root.toPath().relativize(movedFile.toPath());
		Index index = userProfile.getFileByPath(relativePath);

		Assert.assertNotNull(index);

		// check isShared flag
		Assert.assertFalse(index.isShared());

		// check if content protection keys are the default content protection key
		Assert.assertTrue(index.getProtectionKeys().getPrivate()
				.equals(userProfile.getProtectionKeys().getPrivate()));
		Assert.assertTrue(index.getProtectionKeys().getPublic()
				.equals(userProfile.getProtectionKeys().getPublic()));

		// check write access
		Assert.assertTrue(index.canWrite());

		// check user permissions
		Set<String> users = index.getCalculatedUserList();
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(userProfile.getUserId()));

		// check user permissions in case of a folder
		if (movedFile.isDirectory()) {
			Assert.assertTrue(index.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) index).getCalculatedUserPermissions();
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userProfile.getUserId(),
					PermissionType.WRITE)));
		} else {
			Assert.assertTrue(index.isFile());
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
