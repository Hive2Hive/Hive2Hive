package org.hive2hive.core.processes.share.read;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.share.BaseShareReadWriteTest;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#READ} permission. Tests if updates get synchronized among
 * two sharing users.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithReadPermissionUpdateTest extends BaseShareReadWriteTest {

	private static File subFolderA;
	private static File subFolderB;
	private static IFileConfiguration fileConfig;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithReadPermissionUpdateTest.class;
		beforeClass();
	}

	@Before
	public void initTest() throws Exception {
		setupNetworkAndShares(PermissionType.READ);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", rootA.toPath().relativize(subFolderA.toPath()).toString());
		UseCaseTestUtil.uploadNewFile(nodeA, subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronizedAdding(subFolderB);

		fileConfig = FileConfiguration.createDefault();
	}

	@Test
	public void testSynchronizeAddFileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at A.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(nodeA, fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromATryToUpdateAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Try to update file '{}' at B.", fileFromAAtA.toString());
		FileUtils.write(fileFromAAtB, randomString(), false);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createUpdateFileProcess(fileFromAAtB, nodeB,
				fileConfig));
		checkFileIndex(fileFromAAtA, fileFromAAtB, HashUtil.hash(fileFromAAtA));
	}

	@Test
	public void testSynchronizeAddSubfileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at A.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(nodeA, fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromATryToUpdateAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(nodeA, fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Try to update file '{}' at B.", fileFromAAtA.toString());
		FileUtils.write(fileFromAAtB, randomString(), false);
		TestExecutionUtil.executeProcessTillFailed(ProcessFactory.instance().createUpdateFileProcess(fileFromAAtB, nodeB,
				fileConfig));
		checkFileIndex(fileFromAAtA, fileFromAAtB, HashUtil.hash(fileFromAAtA));
	}

	/**
	 * Waits a certain amount of time till a file appears (add).
	 * 
	 * @param synchronizingFile
	 *            the file to synchronize
	 * @param appearing
	 *            <code>true</code> if file should appear, <code>false</code> if file should disappear
	 */
	private static void waitTillSynchronizedAdding(File synchronizingFile) {
		H2HWaiter waiter = new H2HWaiter(40);
		do {
			waiter.tickASecond();
		} while (!synchronizingFile.exists());
	}

	/**
	 * Waits a certain amount of time till a file gets updated.
	 * 
	 * @param synchronizingFile
	 *            the file to synchronize
	 * @param appearing
	 *            <code>true</code> if file should appear, <code>false</code> if file should disappear
	 */
	private static void waitTillSynchronizedUpdating(File updatingFile, long lastModified) {
		H2HWaiter waiter = new H2HWaiter(40);
		do {
			waiter.tickASecond();
		} while (updatingFile.lastModified() == lastModified);
	}

	private void checkFileIndex(File fileA, File fileB, byte[] md5Hash) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = nodeA.getSession().getProfileManager().readUserProfile();
		FileIndex indexA = (FileIndex) userProfileA.getFileByPath(fileA, nodeA.getSession().getRootFile());

		UserProfile userProfileB = nodeB.getSession().getProfileManager().readUserProfile();
		FileIndex indexB = (FileIndex) userProfileB.getFileByPath(fileB, nodeB.getSession().getRootFile());

		// check if index is file
		Assert.assertTrue(indexA.isFile());
		Assert.assertTrue(indexB.isFile());

		// check if isShared flag is set
		Assert.assertTrue(indexA.isShared());
		Assert.assertTrue(indexB.isShared());

		// check write access
		Assert.assertTrue(indexA.canWrite());
		// user B isn't allowed to write
		Assert.assertFalse(indexB.canWrite());

		// check if md5 hash is the same
		Assert.assertTrue(Arrays.equals(indexA.getMD5(), md5Hash));
		Assert.assertTrue(Arrays.equals(indexB.getMD5(), md5Hash));

		// check if userA's content protection keys are other ones
		Assert.assertFalse(indexA.getProtectionKeys().getPrivate().equals(userProfileA.getProtectionKeys().getPrivate()));
		Assert.assertFalse(indexA.getProtectionKeys().getPublic().equals(userProfileA.getProtectionKeys().getPublic()));
		// check if user B has no content protection keys
		Assert.assertNull(indexB.getProtectionKeys());

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
	}
}
