package org.hive2hive.core.processes.share.read;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.share.BaseShareReadWriteTest;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#READ} permission. Tests moving files and folders out of a
 * shared folder.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithReadPermissionMoveOutTest extends BaseShareReadWriteTest {

	private static File subFolderA;
	private static File subFolderB;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithReadPermissionMoveOutTest.class;
		beforeClass();
		setupNetwork();
	}

	@Before
	public void initTest() throws Exception {
		setupShares(PermissionType.READ);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", rootA.toPath().relativize(subFolderA.toPath()));
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file '{}' at A to root folder of A.", fileFromAAtA.toString());
		File movedFileFromAAtA = new File(rootA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till moving of file '{}' to '{}' gets synchronized with B.", fileFromAAtA.toString(),
				movedFileFromAAtA.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndexesAfterMoving(fileFromAAtA, fileFromAAtB, movedFileFromAAtA);
	}

	@Test
	public void testSynchronizeAddFileAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Try to move file '{}' at B to root folder of B.", fileFromAAtA.toString());
		File movingFileAtB = new File(rootB, fileFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingMoving(fileFromAAtA, fileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubFileAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file '{}' at A to root folder of A.", fileFromAAtA.toString());
		File movedFileFromAAtA = new File(rootA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till moving of file '{}' to '{}' gets synchronized with B.", fileFromAAtA.toString(),
				movedFileFromAAtA.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndexesAfterMoving(fileFromAAtA, fileFromAAtB, movedFileFromAAtA);
	}

	@Test
	public void testSynchronizeAddSubfileAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Try to move file '{}' at B to root folder of B.", fileFromAAtA.toString());
		File movingFileAtB = new File(rootB, fileFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingMoving(fileFromAAtA, fileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder '{}' at A to root folder of A.", folderFromAAtA.toString());
		File movedFolderFromAAtA = new File(rootA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moving of folder '{}' to '{}' gets synchronized with B.", folderFromAAtA.toString(),
				movedFolderFromAAtA.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndexesAfterMoving(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA);
	}

	@Test
	public void testSynchronizeAddFolderAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Try to move folder '{}' at B to root folder of B.", folderFromAAtA.toString());
		File movingFolderAtB = new File(rootB, folderFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingMoving(folderFromAAtA, folderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderAtAMoveOutAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder '{}' at A to root folder of A.", folderFromAAtA.toString());
		File movedFolderFromAAtA = new File(rootA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moving of folder '{}' to '{}' gets synchronized with B.", folderFromAAtA.toString(),
				movedFolderFromAAtA.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndexesAfterMoving(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA);
	}

	@Test
	public void testSynchronizeAddSubfolderAtATryToMoveOutAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Try to move folder '{}' at B to root folder of B.", folderFromAAtA.toString());
		File movingFolderAtB = new File(rootB, folderFromAAtA.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingMoving(folderFromAAtA, folderFromAAtB);
	}

	private void checkIndexesAfterMoving(File oldFileA, File oldFileB, File newFileA) throws GetFailedException,
			NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		Index indexOldA = userProfileA.getFileByPath(oldFileA, nodeA.getSession().getRootFile());
		// should have been deleted
		Assert.assertNull(indexOldA);

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		Index indexOldB = userProfileB.getFileByPath(oldFileB, nodeB.getSession().getRootFile());
		// should have been deleted
		Assert.assertNull(indexOldB);

		Index indexNew = userProfileA.getFileByPath(newFileA, nodeA.getSession().getRootFile());
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

	private void checkIndexAfterTryingMoving(File fileA, File fileB) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		Index indexA = userProfileA.getFileByPath(fileA, nodeA.getSession().getRootFile());

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		Index indexB = userProfileB.getFileByPath(fileB, nodeB.getSession().getRootFile());

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
}
