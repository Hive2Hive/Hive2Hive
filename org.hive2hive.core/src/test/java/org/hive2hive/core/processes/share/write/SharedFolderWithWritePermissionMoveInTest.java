package org.hive2hive.core.processes.share.write;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.share.BaseShareReadWriteTest;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests moving files and folder into a
 * shared folder.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithWritePermissionMoveInTest extends BaseShareReadWriteTest {

	private File subFolderA;
	private File subFolderB;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithWritePermissionMoveInTest.class;
		beforeClass();
	}

	@Before
	public void initTest() throws Exception {
		setupNetworkAndShares(PermissionType.WRITE);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder'.");
		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeMoveFileFromAIntoSharedFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'fileFromA' into root folder of A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("fileFromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootA, H2HConstants.DEFAULT_CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Move file 'fileFromA' at A into shared folder 'sharedfolder'.");
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file 'fileFromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromBIntoSharedFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'fileFromB' into root folder of B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("fileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootB, H2HConstants.DEFAULT_CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Move file 'fileFromB' at B into shared folder 'sharedfolder'.");
		File movedFileFromBAtB = new File(sharedFolderB, fileFromBAtB.getName());
		FileUtils.moveFile(fileFromBAtB, movedFileFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved in file 'fileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(movedFileFromBAtA, true);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromAIntoSharedFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'folderFromA' into root folder of A.");
		File folderFromAAtA = new File(rootA, "folderFromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Move folder 'folderFromA' at A into shared folder 'sharedfolder'.");
		File movedFolderFromAAtA = new File(sharedFolderA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder 'folderFromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromBIntoSharedFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'folderFromB' into root folder of B.");
		File folderFromBAtB = new File(rootB, "folderFromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Move folder 'folderFromB' at B into shared folder 'sharedfolder'.");
		File movedFolderFromBAtB = new File(sharedFolderB, folderFromBAtB.getName());
		FileUtils.moveDirectory(folderFromBAtB, movedFolderFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till new moved in folder 'folderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(sharedFolderA, folderFromBAtB.getName());
		waitTillSynchronized(movedFolderFromBAtA, true);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(movedFolderFromBAtA, movedFolderFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromAIntoSharedSubFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'subfileFromA' into root folder of A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfileFromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootA, H2HConstants.DEFAULT_CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Move file 'subfileFromA' at A into shared folder 'sharedfolder/subfolderA'.");
		File movedFileFromAAtA = new File(subFolderA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file 'subfileFromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromBIntoSharedSubFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'subfileFromB' into root folder of B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				rootB, H2HConstants.DEFAULT_CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Move file 'subfileFromB' at B into shared folder 'sharedfolder/subfolder'.");
		File movedFileFromBAtB = new File(subFolderB, fileFromBAtB.getName());
		FileUtils.moveFile(fileFromBAtB, movedFileFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved in file 'subfileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronized(movedFileFromBAtA, true);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromAIntoSharedSubFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'subfolderFromA' into root folder of A.");
		File folderFromAAtA = new File(rootA, "subfolderFromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Move folder 'subfolderFromA' at A into shared folder 'sharedfolder/subfolder'.");
		File movedFolderFromAAtA = new File(subFolderA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder 'subfolderFromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromBIntoSharedSubFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'subfolderFromB' into root folder of B.");
		File folderFromBAtB = new File(rootB, "subfolderFromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Move folder 'subfolderFromB' at B into shared folder 'sharedFolder/subfolder'.");
		File movedFolderFromBAtB = new File(subFolderB, folderFromBAtB.getName());
		FileUtils.moveDirectory(folderFromBAtB, movedFolderFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till new moved in folder 'subfolderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(subFolderA, folderFromBAtB.getName());
		waitTillSynchronized(movedFolderFromBAtA, true);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(movedFolderFromBAtA, movedFolderFromBAtB);
	}

	private void checkIndex(File fileAtA, File fileAtB) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager().readUserProfile();
		Index indexA = userProfileA.getFileByPath(fileAtA, network.get(0).getSession().getRootFile());

		UserProfile userProfileB = network.get(1).getSession().getProfileManager().readUserProfile();
		Index indexB = userProfileB.getFileByPath(fileAtB, network.get(1).getSession().getRootFile());

		// check if content protection keys are the same
		Assert.assertTrue(indexA.getProtectionKeys().getPrivate().equals(indexB.getProtectionKeys().getPrivate()));
		Assert.assertTrue(indexA.getProtectionKeys().getPublic().equals(indexB.getProtectionKeys().getPublic()));

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
}
