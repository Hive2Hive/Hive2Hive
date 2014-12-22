package org.hive2hive.core.processes.share.write;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.share.BaseShareReadWriteTest;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests if updates get synchronized among
 * two sharing users.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithWritePermissionUpdateTest extends BaseShareReadWriteTest {

	private File subFolderA;
	private File subFolderB;

	@Before
	public void initTest() throws Exception {
		setupNetworkAndShares(PermissionType.WRITE);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", subFolderA.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Update file '{}' at A.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromAUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Update file '{}' at B.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtB.lastModified();
		FileUtils.write(fileFromAAtB, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromAAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtA, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromBUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file1FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderB, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from B.", fileFromBAtB.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", fileFromBAtB.toString());
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Update file '{}' at A.", fileFromBAtB.toString());
		long lastUpdated = fileFromBAtA.lastModified();
		FileUtils.write(fileFromBAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromBAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromBAtB.toString());
		waitTillSynchronizedUpdating(fileFromBAtB, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(fileFromBAtA, fileFromBAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromBUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file2FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderB, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from B.", fileFromBAtB.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", fileFromBAtB.toString());
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Update file '{}' at B.", fileFromBAtB.toString());
		long lastUpdated = fileFromBAtB.lastModified();
		FileUtils.write(fileFromBAtB, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromBAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", fileFromBAtB.toString());
		waitTillSynchronizedUpdating(fileFromBAtA, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(fileFromBAtA, fileFromBAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Update file '{}' at A.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromAUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderA, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from A.", fileFromAAtA.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", fileFromAAtA.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Update file '{}' at B.", fileFromAAtA.toString());
		long lastUpdated = fileFromAAtB.lastModified();
		FileUtils.write(fileFromAAtB, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromAAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", fileFromAAtA.toString());
		waitTillSynchronizedUpdating(fileFromAAtA, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(fileFromAAtA, fileFromAAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromBUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile1FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderB, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from B.", fileFromBAtB.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", fileFromBAtB.toString());
		File fileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Update file '{}' at A.", fileFromBAtB.toString());
		long lastUpdated = fileFromBAtA.lastModified();
		FileUtils.write(fileFromBAtA, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromBAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", fileFromBAtB.toString());
		waitTillSynchronizedUpdating(fileFromBAtB, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(fileFromBAtA, fileFromBAtB, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromBUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalArgumentException, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile2FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolderB, H2HConstants.DEFAULT_CHUNK_SIZE);
		logger.info("Upload a new file '{}' from B.", fileFromBAtB.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", fileFromBAtB.toString());
		File fileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Update file '{}' at B.", fileFromBAtB.toString());
		long lastUpdated = fileFromBAtB.lastModified();
		FileUtils.write(fileFromBAtB, randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromBAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", fileFromBAtB.toString());
		waitTillSynchronizedUpdating(fileFromBAtA, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(fileFromBAtA, fileFromBAtB, newMD5);
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
		UserProfile userProfileA = network.get(0).getSession().getProfileManager().readUserProfile();
		FileIndex indexA = (FileIndex) userProfileA.getFileByPath(fileA, network.get(0).getSession().getRootFile());

		UserProfile userProfileB = network.get(1).getSession().getProfileManager().readUserProfile();
		FileIndex indexB = (FileIndex) userProfileB.getFileByPath(fileB, network.get(1).getSession().getRootFile());

		// check if index is file
		Assert.assertTrue(indexA.isFile());
		Assert.assertTrue(indexB.isFile());

		// check if md5 hash is the same
		Assert.assertTrue(Arrays.equals(indexA.getMD5(), md5Hash));
		Assert.assertTrue(Arrays.equals(indexB.getMD5(), md5Hash));

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
	}
}
