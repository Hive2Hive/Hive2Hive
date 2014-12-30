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
 * A folder is shared with {@link PermissionType#READ} permission. Tests moving files and folder into a
 * shared folder.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithReadPermissionMoveInTest extends BaseShareReadWriteTest {
	private static File subFolderA;
	private static File subFolderB;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithReadPermissionMoveInTest.class;
		beforeClass();
	}

	@Before
	public void initTest() throws Exception {
		setupNetworkAndShares(PermissionType.READ);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", rootA.toPath().relativize(subFolderA.toPath()));
		UseCaseTestUtil.uploadNewFile(nodeA, subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeMoveAtAFileAtAIntoSharedFolder() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("fileFromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootA);
		logger.info("Upload a new file '{}' into root folder of A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Move file '{}' at A into shared folder '{}'.", fileFromAAtA.getName(), sharedFolderA.getName());
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file '{}' gets synchronized with B.", movedFileFromAAtA.toString());
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeTryToMoveAtBFileIntoSharedFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("fileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootB);
		logger.info("Upload a new file '{}' into root folder of B.", fileFromBAtB.getName());
		UseCaseTestUtil.uploadNewFile(nodeB, fileFromBAtB);

		logger.info("Try to move file '{}' at B into shared folder '{}'.", fileFromBAtB.getName(), sharedFolderB.getName());
		File movedFileFromBAtB = new File(sharedFolderB, fileFromBAtB.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromBAtB,
				movedFileFromBAtB, nodeB));
	}

	@Test
	public void testSynchronizeMoveAtAFolderAtAIntoSharedFolder() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(rootA, "folderFromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' into root folder of A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Move folder '{}' at A into shared folder '{}'.", folderFromAAtA.getName(), sharedFolderA.getName());
		File movedFolderFromAAtA = new File(sharedFolderA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder '{}' gets synchronized with B.", movedFolderFromAAtA.toString());
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeTryToMoveAtBFolderIntoSharedFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromBAtB = new File(rootB, "folderFromB");
		folderFromBAtB.mkdir();
		logger.info("Upload a new folder '{}' into root folder of B.", folderFromBAtB.getName());
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Try to move folder '{}' at B into shared folder '{}'.", folderFromBAtB.getName(),
				sharedFolderB.getName());
		File movedFolderFromBAtB = new File(sharedFolderB, folderFromBAtB.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromBAtB,
				movedFolderFromBAtB, nodeB));
	}

	@Test
	public void testSynchronizeMoveAtAFileAtAIntoSharedSubfolder() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfileFromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootA);
		logger.info("Upload a new file '{}' into root folder of A.", fileFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Move file '{}' at A into shared folder '{}'.", fileFromAAtA.getName(),
				rootA.toPath().relativize(subFolderA.toPath()));
		File movedFileFromAAtA = new File(subFolderA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file '{}' gets synchronized with B.", movedFileFromAAtA.toString());
		File movedFileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeTryToMoveAtBFileIntoSharedSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootB);
		logger.info("Upload a new file '{}' into root folder of B.", fileFromBAtB.getName());
		UseCaseTestUtil.uploadNewFile(nodeB, fileFromBAtB);

		logger.info("Move file '{}' at B into shared folder '{}'.", fileFromBAtB.getName(),
				rootB.toPath().relativize(subFolderB.toPath()));
		File movedFileFromBAtB = new File(subFolderB, fileFromBAtB.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(fileFromBAtB,
				movedFileFromBAtB, nodeB));
	}

	@Test
	public void testSynchronizeMoveAtAFolderAtAIntoSharedSubfolder() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(rootA, "subfolderFromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' into root folder of A.", folderFromAAtA.getName());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Move folder '{}' at A into shared folder '{}'.", folderFromAAtA.getName(),
				rootA.toPath().relativize(subFolderA.toPath()));
		File movedFolderFromAAtA = new File(subFolderA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(nodeA, folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder '{}' gets synchronized with B.", movedFolderFromAAtA.toString());
		File movedFolderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeTryToMoveAtBFolderIntoSharedSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromBAtB = new File(rootB, "subfolderFromB");
		folderFromBAtB.mkdir();
		logger.info("Upload a new folder '{}' into root folder of B.", folderFromBAtB.getName());
		UseCaseTestUtil.uploadNewFile(nodeB, folderFromBAtB);

		logger.info("Move folder '{}' at B into shared folder '{}'.", folderFromBAtB.getName(),
				rootB.toPath().relativize(subFolderB.toPath()));
		File movedFolderFromBAtB = new File(subFolderB, folderFromBAtB.getName());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createMoveFileProcess(folderFromBAtB,
				movedFolderFromBAtB, nodeB));
	}

	private void checkIndex(File fileA, File fileB) throws GetFailedException, NoSessionException {
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
