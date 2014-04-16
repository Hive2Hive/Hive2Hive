package org.hive2hive.core.test.processes.implementations.share.write;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests bidirectional add and delete
 * scenarios.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionMoveInTest extends H2HJUnitTest {

	private static final IFileConfiguration config = new TestFileConfiguration();
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
		testClass = SharedFolderWithWritePermissionMoveInTest.class;
		beforeClass();

		logger.info("Setup network.");
		network = NetworkTestUtil.createNetwork(networkSize);

		logger.info("Create user A.");
		rootA = NetworkTestUtil.getTempDirectory();
		userA = NetworkTestUtil.generateRandomCredentials();
		logger.info("Register and login user A.");
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		logger.info("Create user B.");
		rootB = NetworkTestUtil.getTempDirectory();
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
		waitTillSynchronized(sharedFolderB);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder'.");
		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB);
	}

	@Test
	public void testSynchronizeMoveFileFromAIntoSharedFolerAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Move file 'fileFromA' at A into shared folder 'sharedfolder'.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("fileFromA",
				new Random().nextInt(maxNumChunks) + 1, rootA, config);
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file 'fileFromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromBIntoSharedFolerAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Move file 'fileFromB' at B into shared folder 'sharedfolder'.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("fileFromB",
				new Random().nextInt(maxNumChunks) + 1, rootB, config);
		File movedFileFromBAtB = new File(sharedFolderB, fileFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved in file 'fileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(movedFileFromBAtA);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(movedFileFromBAtA, movedFileFromBAtB);
	}

	/**
	 * 
	 * Move
	 * ====
	 * Into
	 * --
	 * moveFolderIntoFromA
	 * moveFolderIntoFromB
	 * 
	 * moveFileIntoSubfolderFromA
	 * moveFileIntoSubfolderFromB
	 * moveFolderIntoSubfolderFromA
	 * moveFolderIntoSubfolderFromB
	 * 
	 * Internal
	 * -------
	 * addFileFromAMoveToSubfolderAtA
	 * addFileFromAMoveToSubfolderAtB
	 * addFileFromBMoveToSubfolderAtA
	 * addFileFromBMoveToSubfolderAtB
	 * addSubFileFromAMoveToFolderAtA
	 * addSubFileFromAMoveToFolderAtB
	 * addSubFileFromBMoveToFolderAtA
	 * addSubFileFromBMoveToFolderAtB
	 * 
	 * addFolderFromAMoveToSubfolderAtA
	 * addFolderFromAMoveToSubfolderAtB
	 * addFolderFromBMoveToSubfolderAtA
	 * addFolderFromBMoveToSubfolderAtB
	 * addSubFolderFromAMoveToFolderAtA
	 * addSubFolderFromAMoveToFolderAtB
	 * addSubFolderFromBMoveToFolderAtA
	 * addSubFolderFromBMoveToFolderAtB
	 * 
	 * addSubFileFromAMoveToSubfolderAtA
	 * addSubFileFromAMoveToSubfolderAtB
	 * addSubFileFromBMoveToSubfolderAtA
	 * addSubFileFromBMoveToSubfolderAtB
	 * addSubFolderFromAMoveToSubfolderAtA
	 * addSubFolderFromAMoveToSubfolderAtB
	 * addSubFolderFromBMoveToSubfolderAtA
	 * addSubFolderFromBMoveToSubfolderAtB
	 * 
	 * Update
	 * ======
	 * 
	 */

	/**
	 * Waits a certain amount of time till a file appears.
	 * 
	 * @param synchronizingFile
	 *            the file to synchronize
	 */
	private static void waitTillSynchronized(File synchronizingFile) {
		H2HWaiter waiter = new H2HWaiter(40);
		do {
			waiter.tickASecond();
		} while (!synchronizingFile.exists());
	}

	private static void compareFiles(File originalFile, File synchronizedFile) throws IOException {
		Assert.assertEquals(originalFile.getName(), synchronizedFile.getName());
		if (originalFile.isFile() || synchronizedFile.isFile()) {
			Assert.assertTrue(FileUtils.contentEquals(originalFile, synchronizedFile));
			Assert.assertEquals(FileUtils.readFileToString(originalFile),
					FileUtils.readFileToString(synchronizedFile));
		}
	}

	private static void checkIndex(File fileAtA, File fileAtB) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathA = rootA.toPath().relativize(fileAtA.toPath());
		Index indexA = userProfileA.getFileByPath(relativePathA);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathB = rootA.toPath().relativize(fileAtA.toPath());
		Index indexB = userProfileB.getFileByPath(relativePathB);

		// check if content protection keys are the same
		Assert.assertTrue(indexA.getProtectionKeys().getPrivate()
				.equals(indexB.getProtectionKeys().getPrivate()));
		Assert.assertTrue(indexA.getProtectionKeys().getPublic()
				.equals(indexB.getProtectionKeys().getPublic()));

		// check if isShared flag is set
		Assert.assertTrue(indexA.isShared());
		Assert.assertTrue(indexB.isShared());

		// check write access
		Assert.assertTrue(indexA.canWrite());
		Assert.assertTrue(indexB.canWrite());

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
		if (fileAtA.isDirectory()) {
			Assert.assertTrue(indexA.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) indexA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(indexA.isFile());
		}

		// check user permissions in case of a folder at B
		if (fileAtB.isDirectory()) {
			Assert.assertTrue(indexB.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) indexB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(indexB.isFile());
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
