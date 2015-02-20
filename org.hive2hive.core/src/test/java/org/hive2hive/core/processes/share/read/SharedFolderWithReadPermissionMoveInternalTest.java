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
 * A folder is shared with {@link PermissionType#READ} permission. Tests moving files and folder within a
 * shared folder.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithReadPermissionMoveInternalTest extends BaseShareReadWriteTest {

	private static File subFolder1A;
	private static File subFolder1B;
	private static File subFolder2A;
	private static File subFolder2B;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithReadPermissionDeleteTest.class;
		beforeClass();
		setupNetwork();
	}

	@Before
	public void initTest() throws Exception {
		setupShares(PermissionType.READ);

		subFolder1A = new File(sharedFolderA, "subfolder1");
		subFolder1A.mkdir();
		logger.info("Upload a new subfolder '{}'.", subFolder1A);
		UseCaseTestUtil.uploadNewFile(nodeA, subFolder1A);
		subFolder1B = new File(sharedFolderB, subFolder1A.getName());
		waitTillSynchronized(subFolder1B, true);

		subFolder2A = new File(sharedFolderA, "subfolder2");
		subFolder2A.mkdir();
		logger.info("Upload a new subfolder '{}'.", subFolder2A);
		UseCaseTestUtil.uploadNewFile(nodeA, subFolder2A);
		subFolder2B = new File(sharedFolderB, subFolder2A.getName());
		waitTillSynchronized(subFolder2B, true);
	}

	@Test
	public void testSynchronizeAddFileAtAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movedFileFromAAtA = new File(subFolder1A, fileFromAAtA.getName());
		logger.info("Move file '{}' at A into shared subfolder '{}'.", fileFromAAtA.getName(), subFolder1A);
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File movedFileFromAAtB = new File(subFolder1B, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndexAfterMoving(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileAtATryToMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movingFileAtB = new File(subFolder1B, fileFromAAtA.getName());
		logger.info("Try to move file '{}' at B into shared subfolder '{}'.", fileFromAAtA.getName(), subFolder1B);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingToMove(fileFromAAtA, fileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderAtAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movedFolderFromAAtA = new File(subFolder1A, folderFromAAtA.getName());
		logger.info("Move folder '{}' at A into shared subfolder '{}'.", folderFromAAtA.getName(), subFolder1A);
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File movedFolderFromAAtB = new File(subFolder1B, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndexAfterMoving(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderAtATryToMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movingFolderAtB = new File(subFolder1B, folderFromAAtA.getName());
		logger.info("Try to move folder '{}' at B into shared subfolder '{}'.", folderFromAAtA.getName(), subFolder1B);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingToMove(folderFromAAtA, folderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileAtAMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1A);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(subFolder1B, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		logger.info("Move file '{}' at A into shared subfolder '{}'.", fileFromAAtA.getName(), sharedFolderA);
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndexAfterMoving(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileAtATryToMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1A);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(subFolder1B, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movingFileAtB = new File(sharedFolderB, fileFromAAtA.getName());
		logger.info("Try to move file '{}' at B into shared subfolder '{}'.", fileFromAAtA.getName(), sharedFolderB);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingToMove(fileFromAAtA, fileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderAtAMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolder1A, "subfolder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(subFolder1B, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movedFolderFromAAtA = new File(sharedFolderA, folderFromAAtA.getName());
		logger.info("Move folder '{}' at A into shared subfolder '{}'.", folderFromAAtA.getName(), sharedFolderA);
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndexAfterMoving(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderAtATryToMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolder1A, "subfolder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(subFolder1B, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movingFolderAtB = new File(sharedFolderB, folderFromAAtA.getName());
		logger.info("Try to move folder '{}' at B into shared subfolder '{}'.", folderFromAAtA.getName(), sharedFolderB);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingToMove(folderFromAAtA, folderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubFileAtAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile3FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1A);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(subFolder1B, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movedFileFromAAtA = new File(subFolder2A, fileFromAAtA.getName());
		logger.info("Move file '{}' at A into shared subfolder '{}'.", fileFromAAtA.getName(), subFolder2A);
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File movedFileFromAAtB = new File(subFolder2B, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndexAfterMoving(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileAtATryToMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile4FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1A);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.getName());
		File fileFromAAtB = new File(subFolder1B, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		File movingFileAtB = new File(subFolder2B, fileFromAAtA.getName());
		logger.info("Try to move file '{}' at B into shared subfolder '{}'.", fileFromAAtA.getName(), subFolder2B);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromAAtB,
				movingFileAtB, nodeB));
		checkIndexAfterTryingToMove(fileFromAAtA, fileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderAtAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolder1A, "subfolder3FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(subFolder1B, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movedFolderFromAAtA = new File(subFolder2A, folderFromAAtA.getName());
		logger.info("Move folder '{}' at A into shared subfolder '{}'.", folderFromAAtA.getName(), subFolder2A);
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File movedFolderFromAAtB = new File(subFolder2B, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndexAfterMoving(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderAtATryToMoveToSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolder1A, "subfolder4FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.getName());
		File folderFromAAtB = new File(subFolder1B, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		File movingFolderAtB = new File(subFolder2B, folderFromAAtA.getName());
		logger.info("Try to move folder '{}' at B into shared subfolder '{}'.", folderFromAAtA.getName(), subFolder2B);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromAAtB,
				movingFolderAtB, nodeB));
		checkIndexAfterTryingToMove(folderFromAAtA, folderFromAAtB);
	}

	private void checkIndexAfterMoving(File oldFileA, File oldFileB, File newFileA, File newFileB)
			throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		Index oldIndexAtA = userProfileA.getFileByPath(oldFileA, nodeA.getSession().getRootFile());
		Index newIndexAtA = userProfileA.getFileByPath(newFileA, nodeA.getSession().getRootFile());

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		Index oldIndexAtB = userProfileB.getFileByPath(oldFileB, nodeB.getSession().getRootFile());
		Index newIndexAtB = userProfileB.getFileByPath(newFileB, nodeB.getSession().getRootFile());

		// check if old indexes have been removed
		Assert.assertNull(oldIndexAtA);
		Assert.assertNull(oldIndexAtB);

		// check if userA's content protection keys are other ones
		Assert.assertFalse(newIndexAtA.getProtectionKeys().getPrivate()
				.equals(userProfileA.getProtectionKeys().getPrivate()));
		Assert.assertFalse(newIndexAtA.getProtectionKeys().getPublic().equals(userProfileA.getProtectionKeys().getPublic()));
		// check if user B has no content protection keys
		Assert.assertNull(newIndexAtB.getProtectionKeys());

		// check if isShared flag is set
		Assert.assertTrue(newIndexAtA.isShared());
		Assert.assertTrue(newIndexAtB.isShared());

		// check write access
		Assert.assertTrue(newIndexAtA.canWrite());
		// user B has no write access
		Assert.assertFalse(newIndexAtB.canWrite());

		// check user permissions at A
		Set<String> usersA = newIndexAtA.getCalculatedUserList();
		Assert.assertEquals(2, usersA.size());
		Assert.assertTrue(usersA.contains(userA.getUserId()));
		Assert.assertTrue(usersA.contains(userB.getUserId()));

		// check user permissions at A
		Set<String> usersB = newIndexAtB.getCalculatedUserList();
		Assert.assertEquals(2, usersB.size());
		Assert.assertTrue(usersB.contains(userA.getUserId()));
		Assert.assertTrue(usersB.contains(userB.getUserId()));

		// check user permissions in case of a folder at A
		if (newIndexAtA.isFolder()) {
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
		}

		// check user permissions in case of a folder at B
		if (newIndexAtB.isFolder()) {
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
		}
	}

	private void checkIndexAfterTryingToMove(File fileA, File fileB) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		Index indexAtA = userProfileA.getFileByPath(fileA, nodeA.getSession().getRootFile());

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		Index indexAtB = userProfileB.getFileByPath(fileB, nodeB.getSession().getRootFile());

		// check if userA's content protection keys are other ones
		Assert.assertFalse(indexAtA.getProtectionKeys().getPrivate().equals(userProfileA.getProtectionKeys().getPrivate()));
		Assert.assertFalse(indexAtA.getProtectionKeys().getPublic().equals(userProfileA.getProtectionKeys().getPublic()));
		// check if user B has no content protection keys
		Assert.assertNull(indexAtB.getProtectionKeys());

		// check if isShared flag is set
		Assert.assertTrue(indexAtA.isShared());
		Assert.assertTrue(indexAtB.isShared());

		// check write access
		Assert.assertTrue(indexAtA.canWrite());
		// user B has no write access
		Assert.assertFalse(indexAtB.canWrite());

		// check user permissions at A
		Set<String> usersA = indexAtA.getCalculatedUserList();
		Assert.assertEquals(2, usersA.size());
		Assert.assertTrue(usersA.contains(userA.getUserId()));
		Assert.assertTrue(usersA.contains(userB.getUserId()));

		// check user permissions at A
		Set<String> usersB = indexAtB.getCalculatedUserList();
		Assert.assertEquals(2, usersB.size());
		Assert.assertTrue(usersB.contains(userA.getUserId()));
		Assert.assertTrue(usersB.contains(userB.getUserId()));

		// check user permissions in case of a folder at A
		if (indexAtA.isFolder()) {
			Set<UserPermission> permissions = ((FolderIndex) indexAtA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
		}

		// check user permissions in case of a folder at B
		if (indexAtB.isFolder()) {
			Set<UserPermission> permissions = ((FolderIndex) indexAtB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.READ)));
		}
	}
}
