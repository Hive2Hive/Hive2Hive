package org.hive2hive.core.processes.share.write;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
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
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests if updates get synchronized among
 * two sharing users.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionUpdateTest extends H2HJUnitTest {

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
		testClass = SharedFolderWithWritePermissionUpdateTest.class;
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

		sharedFolderA = new File(rootA, "sharedfolder");
		sharedFolderA.mkdirs();
		logger.info("Upload folder '{}' from A.", sharedFolderA.getName());
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderA);

		logger.info("Share folder '{}' with user B giving write permission.", sharedFolderA.getName());
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.WRITE);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronizedAdding(sharedFolderB);

		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		logger.info("Upload a new subfolder '{}'.", rootA.toPath().relativize(subFolderA.toPath()).toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronizedAdding(subFolderB);
	}

	@Test
	public void testSynchronizeAddFileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' from A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at A.", relativePath);
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromAUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' from A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at B.", relativePath);
		long lastUpdated = fileFromAAtB.lastModified();
		FileUtils.write(fileFromAAtB, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromAAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromAAtA, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromBUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file1FromB", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderB, CHUNK_SIZE);
		Path relativePath = rootB.toPath().relativize(fileFromBAtB.toPath());
		logger.info("Upload a new file '{}' from B.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", relativePath.toString());
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronizedAdding(fileFromBAtA);

		logger.info("Update file '{}' at A.", relativePath);
		long lastUpdated = fileFromBAtA.lastModified();
		FileUtils.write(fileFromBAtA, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromBAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromBAtB, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddFileFromBUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file2FromB", new Random().nextInt(maxNumChunks) + 1,
				sharedFolderB, CHUNK_SIZE);
		Path relativePath = rootB.toPath().relativize(fileFromBAtB.toPath());
		logger.info("Upload a new file '{}' from B.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", relativePath.toString());
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronizedAdding(fileFromBAtA);

		logger.info("Update file '{}' at B.", relativePath);
		long lastUpdated = fileFromBAtB.lastModified();
		FileUtils.write(fileFromBAtB, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromBAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromBAtA, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromAUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(maxNumChunks) + 1,
				subFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' from A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at A.", relativePath);
		long lastUpdated = fileFromAAtA.lastModified();
		FileUtils.write(fileFromAAtA, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromAAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromAAtB, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromAUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(maxNumChunks) + 1,
				subFolderA, CHUNK_SIZE);
		Path relativePath = rootA.toPath().relativize(fileFromAAtA.toPath());
		logger.info("Upload a new file '{}' from A.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file '{}' gets synchronized with B.", relativePath.toString());
		File fileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronizedAdding(fileFromAAtB);

		logger.info("Update file '{}' at B.", relativePath);
		long lastUpdated = fileFromAAtB.lastModified();
		FileUtils.write(fileFromAAtB, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromAAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromAAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromAAtA, lastUpdated);
		compareFiles(fileFromAAtA, fileFromAAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromBUpdateAtA() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile1FromB", new Random().nextInt(maxNumChunks) + 1,
				subFolderB, CHUNK_SIZE);
		Path relativePath = rootB.toPath().relativize(fileFromBAtB.toPath());
		logger.info("Upload a new file '{}' from B.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", relativePath.toString());
		File fileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronizedAdding(fileFromBAtA);

		logger.info("Update file '{}' at A.", relativePath);
		long lastUpdated = fileFromBAtA.lastModified();
		FileUtils.write(fileFromBAtA, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtA);
		UseCaseTestUtil.uploadNewVersion(network.get(0), fileFromBAtA);

		logger.info("Wait till update of file '{}' gets synchronized with B.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromBAtB, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(relativePath, newMD5);
	}

	@Test
	public void testSynchronizeAddSubfileFromBUpdateAtB() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile2FromB", new Random().nextInt(maxNumChunks) + 1,
				subFolderB, CHUNK_SIZE);
		Path relativePath = rootB.toPath().relativize(fileFromBAtB.toPath());
		logger.info("Upload a new file '{}' from B.", relativePath.toString());
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file '{}' gets synchronized with A.", relativePath.toString());
		File fileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronizedAdding(fileFromBAtA);

		logger.info("Update file '{}' at B.", relativePath);
		long lastUpdated = fileFromBAtB.lastModified();
		FileUtils.write(fileFromBAtB, NetworkTestUtil.randomString(), false);
		byte[] newMD5 = HashUtil.hash(fileFromBAtB);
		UseCaseTestUtil.uploadNewVersion(network.get(1), fileFromBAtB);

		logger.info("Wait till update of file '{}' gets synchronized with A.", relativePath.toString());
		waitTillSynchronizedUpdating(fileFromBAtA, lastUpdated);
		compareFiles(fileFromBAtA, fileFromBAtB);
		checkFileIndex(relativePath, newMD5);
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

	private static void compareFiles(File originalFile, File synchronizedFile) throws IOException {
		Assert.assertEquals(originalFile.getName(), synchronizedFile.getName());
		Assert.assertTrue(FileUtils.contentEquals(originalFile, synchronizedFile));
	}

	private static void checkFileIndex(Path relativePath, byte[] md5Hash) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		FileIndex indexA = (FileIndex) userProfileA.getFileByPath(relativePath);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		FileIndex indexB = (FileIndex) userProfileB.getFileByPath(relativePath);

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

	@AfterClass
	public static void endTest() throws IOException {
		FileUtils.deleteDirectory(rootA);
		FileUtils.deleteDirectory(rootB);

		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
