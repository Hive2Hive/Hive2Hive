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
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests bidirectional add and delete
 * scenarios.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionDeleteTest extends H2HJUnitTest {

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
		testClass = SharedFolderWithWritePermissionDeleteTest.class;
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

		logger.info("Upload folder 'folder1' from A.");
		sharedFolderA = new File(rootA, "folder1");
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderA);

		logger.info("Share folder 'folder1' with user B giving write permission.");
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.WRITE);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB, true);

		logger.info("Upload a new subfolder 'folder1/subfolder'.");
		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileFromADeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file1FromA' from A.");
		File file1FromAAtA = FileTestUtil.createFileRandomContent("file1FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1FromAAtA);

		logger.info("Wait till new file 'folder1/file1FromA' gets synchronized with B.");
		File file1FromAAtB = new File(sharedFolderB, file1FromAAtA.getName());
		waitTillSynchronized(file1FromAAtB, true);
		compareFiles(file1FromAAtA, file1FromAAtB);
		checkIndex(file1FromAAtA, file1FromAAtB, false);

		logger.info("Delete file 'folder1/file1FromA' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), file1FromAAtA);

		logger.info("Wait till deletion of file 'folder1/file1FromA' gets synchronized with B.");
		waitTillSynchronized(file1FromAAtB, false);
		checkIndex(file1FromAAtA, file1FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddFileFromADeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file2FromA' from A.");
		File file2FromAAtA = FileTestUtil.createFileRandomContent("file2FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), file2FromAAtA);

		logger.info("Wait till new file 'folder1/file2FromA' gets synchronized with B.");
		File file2FromAAtB = new File(sharedFolderB, file2FromAAtA.getName());
		waitTillSynchronized(file2FromAAtB, true);
		compareFiles(file2FromAAtA, file2FromAAtB);
		checkIndex(file2FromAAtA, file2FromAAtB, false);

		logger.info("Delete file 'folder1/file2FromA' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), file2FromAAtB);

		logger.info("Wait till deletion of file 'folder1/file2FromA' gets synchronized with A.");
		waitTillSynchronized(file2FromAAtA, false);
		checkIndex(file2FromAAtA, file2FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddFileFromBDeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file1FromB' from B.");
		File file1FromBAtB = FileTestUtil.createFileRandomContent("file1FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), file1FromBAtB);

		logger.info("Wait till new file 'folder1/file1FromB' gets synchronized with A.");
		File file1FromBAtA = new File(sharedFolderA, file1FromBAtB.getName());
		waitTillSynchronized(file1FromBAtA, true);
		compareFiles(file1FromBAtA, file1FromBAtB);
		checkIndex(file1FromBAtA, file1FromBAtB, false);

		logger.info("Delete file 'folder1/file1FromB' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), file1FromBAtB);

		logger.info("Wait till deletion of file 'folder1/file1FromB' gets synchronized with A.");
		waitTillSynchronized(file1FromBAtA, false);
		checkIndex(file1FromBAtA, file1FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddFileFromBDeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file2FromB' from B.");
		File file2FromBAtB = FileTestUtil.createFileRandomContent("file2FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2FromBAtB);

		logger.info("Wait till new file 'folder1/file2FromB' gets synchronized with A.");
		File file2FromBAtA = new File(sharedFolderA, file2FromBAtB.getName());
		waitTillSynchronized(file2FromBAtA, true);
		compareFiles(file2FromBAtA, file2FromBAtB);
		checkIndex(file2FromBAtA, file2FromBAtB, false);

		logger.info("Delete file 'folder1/file2FromB' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), file2FromBAtA);

		logger.info("Wait till deletion of file 'folder1/file2FromB' gets synchronized with B.");
		waitTillSynchronized(file2FromBAtB, false);
		checkIndex(file2FromBAtA, file2FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromADeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfolder 'folder1/subfolder1FromA' from A.");
		File subFolder1FromAAtA = new File(sharedFolderA, "subfolder1FromA");
		subFolder1FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1FromAAtA);

		logger.info("Wait till new subfolder 'folder1/subfolder1FromA' gets synchronized with B.");
		File subFolder1FromAAtB = new File(sharedFolderB, subFolder1FromAAtA.getName());
		waitTillSynchronized(subFolder1FromAAtB, true);
		compareFiles(subFolder1FromAAtA, subFolder1FromAAtB);
		checkIndex(subFolder1FromAAtA, subFolder1FromAAtB, false);

		logger.info("Delete subfolder 'folder1/subfolder1FromA' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subFolder1FromAAtA);

		logger.info("Wait till deletion of subfolder 'folder1/subfolder1FromA' gets synchronized with B.");
		waitTillSynchronized(subFolder1FromAAtB, false);
		checkIndex(subFolder1FromAAtA, subFolder1FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromADeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfolder 'folder1/subfolder2FromA' from A.");
		File subFolder2FromAAtA = new File(sharedFolderA, "subfolder2FromA");
		subFolder2FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder2FromAAtA);

		logger.info("Wait till new subfolder 'folder1/subfolder2FromA' gets synchronized with B.");
		File subFolder2FromAAtB = new File(sharedFolderB, subFolder2FromAAtA.getName());
		waitTillSynchronized(subFolder2FromAAtB, true);
		compareFiles(subFolder2FromAAtA, subFolder2FromAAtB);
		checkIndex(subFolder2FromAAtA, subFolder2FromAAtB, false);

		logger.info("Delete subfolder 'folder1/subfolder2FromA' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subFolder2FromAAtB);

		logger.info("Wait till deletion of subfolder 'folder1/subfolder2FromA' gets synchronized with A.");
		waitTillSynchronized(subFolder2FromAAtA, false);
		checkIndex(subFolder2FromAAtA, subFolder2FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromBDeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfolder 'folder1/subfolder1FromB' from B.");
		File subFolder1FromBAtB = new File(sharedFolderB, "subfolder1FromB");
		subFolder1FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subFolder1FromBAtB);

		logger.info("Wait till new subfolder 'folder1/subfolder1FromB' gets synchronized with B.");
		File subFolder1FromBAtA = new File(sharedFolderA, subFolder1FromBAtB.getName());
		waitTillSynchronized(subFolder1FromBAtA, true);
		compareFiles(subFolder1FromBAtA, subFolder1FromBAtB);
		checkIndex(subFolder1FromBAtA, subFolder1FromBAtB, false);

		logger.info("Delete subfolder 'folder1/subfolder1FromB' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subFolder1FromBAtA);

		logger.info("Wait till deletion of subfolder 'folder1/subfolder1FromB' gets synchronized with B.");
		waitTillSynchronized(subFolder1FromBAtB, false);
		checkIndex(subFolder1FromBAtA, subFolder1FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFolderFromBDeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subfolder 'folder1/subfolder2FromB' from B.");
		File subFolder2FromBAtB = new File(sharedFolderB, "subfolder2FromB");
		subFolder2FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subFolder2FromBAtB);

		logger.info("Wait till new subfolder 'folder1/subfolder2FromB' gets synchronized with A.");
		File subFolder2FromBAtA = new File(sharedFolderA, subFolder2FromBAtB.getName());
		waitTillSynchronized(subFolder2FromBAtA, true);
		compareFiles(subFolder2FromBAtA, subFolder2FromBAtB);
		checkIndex(subFolder2FromBAtA, subFolder2FromBAtB, false);

		logger.info("Delete subfolder 'folder1/subfolder2FromB' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subFolder2FromBAtB);

		logger.info("Wait till deletion of subfolder 'folder1/subfolder2FromB' gets synchronized with A.");
		waitTillSynchronized(subFolder2FromBAtA, false);
		checkIndex(subFolder2FromBAtA, subFolder2FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromADeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/subfolder/file1FromA' from A.");
		File subFile1FromAAtA = FileTestUtil.createFileRandomContent("file1FromA",
				new Random().nextInt(maxNumChunks) + 1, subFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), subFile1FromAAtA);

		logger.info("Wait till new file 'folder1/subfolder/file1FromA' gets synchronized with B.");
		File subFile1FromAAtB = new File(subFolderB, subFile1FromAAtA.getName());
		waitTillSynchronized(subFile1FromAAtB, true);
		compareFiles(subFile1FromAAtA, subFile1FromAAtB);
		checkIndex(subFile1FromAAtA, subFile1FromAAtB, false);

		logger.info("Delete file 'folder1/subfolder/file1FromA' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subFile1FromAAtA);

		logger.info("Wait till deletion of file 'folder1/subfolder/file1FromA' gets synchronized with B.");
		waitTillSynchronized(subFile1FromAAtB, false);
		checkIndex(subFile1FromAAtA, subFile1FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromADeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/subfolder/file2FromA' from A.");
		File subFile2FromAAtA = FileTestUtil.createFileRandomContent("file2FromA",
				new Random().nextInt(maxNumChunks) + 1, subFolderA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), subFile2FromAAtA);

		logger.info("Wait till new file 'folder1/subfolder/file2FromA' gets synchronized with B.");
		File subFile2FromAAtB = new File(subFolderB, subFile2FromAAtA.getName());
		waitTillSynchronized(subFile2FromAAtB, true);
		compareFiles(subFile2FromAAtA, subFile2FromAAtB);
		checkIndex(subFile2FromAAtA, subFile2FromAAtB, false);

		logger.info("Delete file 'folder1/subfolder/file2FromA' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subFile2FromAAtB);

		logger.info("Wait till deletion of file 'folder1/subfolder/file2FromA' gets synchronized with A.");
		waitTillSynchronized(subFile2FromAAtA, false);
		checkIndex(subFile2FromAAtA, subFile2FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromBDeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/subfolder/file1FromB' from B.");
		File subFile1FromBAtB = FileTestUtil.createFileRandomContent("file1FromB",
				new Random().nextInt(maxNumChunks) + 1, subFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), subFile1FromBAtB);

		logger.info("Wait till new file 'folder1/subfolder/file1FromB' gets synchronized with A.");
		File subFile1FromBAtA = new File(subFolderA, subFile1FromBAtB.getName());
		waitTillSynchronized(subFile1FromBAtA, true);
		compareFiles(subFile1FromBAtA, subFile1FromBAtB);
		checkIndex(subFile1FromBAtA, subFile1FromBAtB, false);

		logger.info("Delete file 'folder1/subfolder/file1FromB' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subFile1FromBAtA);

		logger.info("Wait till deletion of file 'folder1/subfolder/file1FromB' gets synchronized with B.");
		waitTillSynchronized(subFile1FromBAtB, false);
		checkIndex(subFile1FromBAtA, subFile1FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubFileFromBDeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/subfolder/file2FromB' from B.");
		File subFile2FromBAtB = FileTestUtil.createFileRandomContent("file2FromB",
				new Random().nextInt(maxNumChunks) + 1, subFolderB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), subFile2FromBAtB);

		logger.info("Wait till new file 'folder1/subfolder/file2FromB' gets synchronized with A.");
		File subFile2FromBAtA = new File(subFolderA, subFile2FromBAtB.getName());
		waitTillSynchronized(subFile2FromBAtA, true);
		compareFiles(subFile2FromBAtA, subFile2FromBAtB);
		checkIndex(subFile2FromBAtA, subFile2FromBAtB, false);

		logger.info("Delete file 'folder1/subfolder/file2FromB' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subFile2FromBAtB);

		logger.info("Wait till deletion of file 'folder1/subfolder/file2FromB' gets synchronized with A.");
		waitTillSynchronized(subFile2FromBAtA, false);
		checkIndex(subFile2FromBAtA, subFile2FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubSubFolderFromADeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subsubfolder 'folder1/subfolder/subsubfolder1FromA' from A.");
		File subsubFolder1FromAAtA = new File(subFolderA, "subsubfolder1FromA");
		subsubFolder1FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subsubFolder1FromAAtA);

		logger.info("Wait till new subsubfolder 'folder1/subfolder/subsubfolder1FromA' gets synchronized with B.");
		File subsubFolder1FromAAtB = new File(subFolderB, subsubFolder1FromAAtA.getName());
		waitTillSynchronized(subsubFolder1FromAAtB, true);
		compareFiles(subsubFolder1FromAAtA, subsubFolder1FromAAtB);
		checkIndex(subsubFolder1FromAAtA, subsubFolder1FromAAtB, false);

		logger.info("Delete subsubfolder 'folder1/subfolder/subsubfolder1FromA' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subsubFolder1FromAAtA);

		logger.info("Wait till deletion of subsubfolder 'folder1/subfolder/subsubfolder1FromA' gets synchronized with B.");
		waitTillSynchronized(subsubFolder1FromAAtB, false);
		checkIndex(subsubFolder1FromAAtA, subsubFolder1FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubSubFolderFromADeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subsubfolder 'folder1/subfolder/subsubfolder2FromA' from A.");
		File subsubFolder2FromAAtA = new File(subFolderA, "subsubfolder2FromA");
		subsubFolder2FromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subsubFolder2FromAAtA);

		logger.info("Wait till new folder 'folder1/subfolder/subsubfolder2FromA' gets synchronized with B.");
		File subsubFolder2FromAAtB = new File(subFolderB, subsubFolder2FromAAtA.getName());
		waitTillSynchronized(subsubFolder2FromAAtB, true);
		compareFiles(subsubFolder2FromAAtA, subsubFolder2FromAAtB);
		checkIndex(subsubFolder2FromAAtA, subsubFolder2FromAAtB, false);

		logger.info("Delete folder 'folder1/subfolder/subsubfolder2FromA' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subsubFolder2FromAAtB);

		logger.info("Wait till deletion of folder 'folder1/subfolder/subsubfolder2FromA' gets synchronized with A.");
		waitTillSynchronized(subsubFolder2FromAAtA, false);
		checkIndex(subsubFolder2FromAAtA, subsubFolder2FromAAtB, true);
	}

	@Test
	public void testSynchronizeAddSubSubFolderFromBDeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new subsubfolder 'folder1/subfolder/subsubfolder1FromB' from B.");
		File subsubFolder1FromBAtB = new File(subFolderB, "subsubfolder1FromB");
		subsubFolder1FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subsubFolder1FromBAtB);

		logger.info("Wait till new folder 'folder1/subfolder/subsubfolder1FromB' gets synchronized with B.");
		File subsubFolder1FromBAtA = new File(subFolderA, subsubFolder1FromBAtB.getName());
		waitTillSynchronized(subsubFolder1FromBAtA, true);
		compareFiles(subsubFolder1FromBAtA, subsubFolder1FromBAtB);
		checkIndex(subsubFolder1FromBAtA, subsubFolder1FromBAtB, false);

		logger.info("Delete folder 'folder1/subfolder/subfolder1FromB' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), subsubFolder1FromBAtA);

		logger.info("Wait till deletion of folder 'folder1/subfolder/subfolder1FromB' gets synchronized with B.");
		waitTillSynchronized(subsubFolder1FromBAtB, false);
		checkIndex(subsubFolder1FromBAtA, subsubFolder1FromBAtB, true);
	}

	@Test
	public void testSynchronizeAddSubSubFolderFromBDeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'folder1/subfolder/subsubfolder2FromB' from B.");
		File subsubFolder2FromBAtB = new File(subFolderB, "subsubfolder2FromB");
		subsubFolder2FromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subsubFolder2FromBAtB);

		logger.info("Wait till new folder 'folder1/subfolder/subsubfolder2FromB' gets synchronized with A.");
		File subsubFolder2FromBAtA = new File(subFolderA, subsubFolder2FromBAtB.getName());
		waitTillSynchronized(subsubFolder2FromBAtA, true);
		compareFiles(subsubFolder2FromBAtA, subsubFolder2FromBAtB);
		checkIndex(subsubFolder2FromBAtA, subsubFolder2FromBAtB, false);

		logger.info("Delete folder 'folder1/subfolder/subsubfolder2FromB' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), subsubFolder2FromBAtB);

		logger.info("Wait till deletion of folder 'folder1/subfolder/subsubfolder2FromB' gets synchronized with A.");
		waitTillSynchronized(subsubFolder2FromBAtA, false);
		checkIndex(subsubFolder2FromBAtA, subsubFolder2FromBAtB, true);
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

	private static void compareFiles(File originalFile, File synchronizedFile) throws IOException {
		Assert.assertEquals(originalFile.getName(), synchronizedFile.getName());
		if (originalFile.isFile() || synchronizedFile.isFile()) {
			Assert.assertTrue(FileUtils.contentEquals(originalFile, synchronizedFile));
			Assert.assertEquals(FileUtils.readFileToString(originalFile),
					FileUtils.readFileToString(synchronizedFile));
		}
	}

	private static void checkIndex(File fileAtA, File fileAtB, boolean deleted) throws GetFailedException,
			NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathA = rootA.toPath().relativize(fileAtA.toPath());
		Index indexA = userProfileA.getFileByPath(relativePathA);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathB = rootB.toPath().relativize(fileAtB.toPath());
		Index indexB = userProfileB.getFileByPath(relativePathB);

		// in case of deletion verify removed index nodes
		if (deleted) {
			Assert.assertNull(indexA);
			Assert.assertNull(indexB);
			return;
		}

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
