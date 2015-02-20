package org.hive2hive.core.processes.share.read;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
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
import org.hive2hive.core.utils.TestFileConfiguration;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#READ} permission. Tests bidirectional add and delete
 * scenarios.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithReadPermissionDeleteTest extends BaseShareReadWriteTest {

	private File subFolderA;
	private File subFolderB;

	private IFileConfiguration fileConfig;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithReadPermissionDeleteTest.class;
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

		fileConfig = new TestFileConfiguration();
	}

	@Test
	public void testSynchronizeAddFileAtADeleteAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);

		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, false);

		logger.info("Delete file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.deleteFile(nodeA, fileFromAAtA);

		logger.info("Wait till deletion of file '{}' gets synchronized with B.", fileFromAAtB.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndex(fileFromAAtA, fileFromAAtB, true);
	}

	@Test
	public void testSynchronizeAddFileAtATryToDeleteAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, false);

		logger.info("Try to delete file '{}' at B.", fileFromAAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createDeleteFileProcess(fileFromAAtB, nodeB));
		checkIndex(fileFromAAtA, fileFromAAtB, false);
	}

	@Test
	public void testSynchronizeTryToAddFileAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileAtB = FileTestUtil.createFileRandomContent("fileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderB);
		logger.info("Try to upload a new file '{}' from B.", fileAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance()
				.createAddFileProcess(fileAtB, nodeB, fileConfig));
	}

	@Test
	public void testSynchronizeAddFolderFromADeleteFromA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);
		compareFiles(folderFromAAtA, folderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, false);

		logger.info("Delete folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.deleteFile(nodeA, folderFromAAtA);

		logger.info("Wait till deletion of folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndex(folderFromAAtA, folderFromAAtB, true);
	}

	@Test
	public void testSynchronizeAddFolderAtATryToDeleteAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(sharedFolderA, "folder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);
		compareFiles(folderFromAAtA, folderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, false);

		logger.info("Try to delete folder '{}' at B.", folderFromAAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createDeleteFileProcess(folderFromAAtB, nodeB));
		checkIndex(folderFromAAtA, folderFromAAtB, false);
	}

	@Test
	public void testSynchronizeTryToAddFolderAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderAtB = new File(sharedFolderB, "folderFromB");
		folderAtB.mkdir();
		logger.info("Try to upload a new folder '{}' from B.", folderAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createAddFileProcess(folderAtB, nodeB,
				fileConfig));
	}

	@Test
	public void testSynchronizeAddSubfileAtADeleteAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA);
		logger.info("Upload a new file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, false);

		logger.info("Delete file '{}' at A.", fileFromAAtA.toString());
		UseCaseTestUtil.deleteFile(nodeA, fileFromAAtA);

		logger.info("Wait till deletion of file '{}' gets synchronized with B.", fileFromAAtA.toString());
		waitTillSynchronized(fileFromAAtB, false);
		checkIndex(fileFromAAtA, fileFromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubfileAtATryToDeleteAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, false);

		logger.info("Try to delete file '{}' at B.", fileFromAAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createDeleteFileProcess(fileFromAAtB, nodeB));
		checkIndex(fileFromAAtA, fileFromAAtB, false);
	}

	@Test
	public void testSynchronizeTryToAddSubfileAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileAtB = FileTestUtil.createFileRandomContent("subfileFromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderB);
		logger.info("Try to upload a new file '{}' from B.", fileAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance()
				.createAddFileProcess(fileAtB, nodeB, fileConfig));
	}

	@Test
	public void testSynchronizeAddSubfolderFromADeleteFromA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder1FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);
		compareFiles(folderFromAAtA, folderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, false);

		logger.info("Delete folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.deleteFile(nodeA, folderFromAAtA);

		logger.info("Wait till deletion of folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		waitTillSynchronized(folderFromAAtB, false);
		checkIndex(folderFromAAtA, folderFromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubfolderAtATryToDeleteAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderFromAAtA = new File(subFolderA, "subfolder2FromA");
		folderFromAAtA.mkdir();
		logger.info("Upload a new folder '{}' at A.", folderFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, folderFromAAtA);

		logger.info("Wait till new folder '{}' gets synchronized with B.", folderFromAAtA.toString());
		File folderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);
		compareFiles(folderFromAAtA, folderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, false);

		logger.info("Try to delete folder '{}' at B.", folderFromAAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createDeleteFileProcess(folderFromAAtB, nodeB));
		checkIndex(folderFromAAtA, folderFromAAtB, false);
	}

	@Test
	public void testSynchronizeTryToAddSubfolderAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File folderAtB = new File(subFolderB, "subfolderFromB");
		folderAtB.mkdir();
		logger.info("Try to upload a new folder '{}' from B.", folderAtB.toString());
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createAddFileProcess(folderAtB, nodeB,
				fileConfig));
	}

	private void checkIndex(File fileA, File fileB, boolean deleted) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		Index indexA = userProfileA.getFileByPath(fileA, nodeA.getSession().getRootFile());

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		Index indexB = userProfileB.getFileByPath(fileB, nodeB.getSession().getRootFile());

		// in case of deletion verify removed index nodes
		if (deleted) {
			Assert.assertNull(indexA);
			Assert.assertNull(indexB);
			return;
		} else {
			Assert.assertNotNull(indexA);
			Assert.assertNotNull(indexB);
		}

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
