package org.hive2hive.core.test.processes.implementations.share.write;

import java.io.File;
import java.io.IOException;
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
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests moving files and folder within a
 * shared folder.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionMoveInternalTest extends H2HJUnitTest {

	private static final IFileConfiguration config = new TestFileConfiguration();
	private static final int networkSize = 3;
	private static final int maxNumChunks = 2;
	private static List<NetworkManager> network;

	private static File rootA;
	private static File rootB;
	private static File sharedFolderA;
	private static File sharedFolderB;
	private static File subFolder1AtA;
	private static File subFolder1AtB;
	private static File subFolder2AtA;
	private static File subFolder2AtB;

	private static UserCredentials userA;
	private static UserCredentials userB;

	/**
	 * Setup network. Setup two users with each one client, log them in.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SharedFolderWithWritePermissionMoveInternalTest.class;
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

		logger.info("Upload a new subfolder 'sharedfolder/subfolder1'.");
		subFolder1AtA = new File(sharedFolderA, "subfolder1");
		subFolder1AtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1AtA);
		subFolder1AtB = new File(sharedFolderB, subFolder1AtA.getName());
		waitTillSynchronized(subFolder1AtB);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder2'.");
		subFolder2AtA = new File(sharedFolderA, "subfolder2");
		subFolder2AtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder2AtA);
		subFolder2AtB = new File(sharedFolderB, subFolder2AtA.getName());
		waitTillSynchronized(subFolder2AtB);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveToSubfolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file1FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/file1FromA' gets synchronized with B.");
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB);

		logger.info("Move file 'fileFromA' at A into shared subfolder 'sharedfolder/subfolder'.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'file1FromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveToSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file2FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/file2FromA' gets synchronized with B.");
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB);

		logger.info("Move file 'file2FromA' at B into shared subfolder 'sharedfolder/subfolder'.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(1), fileFromAAtB, movedFileFromAAtB);

		logger.info("Wait till new moved file 'file2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromAAtB.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveToSubfolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file1FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file1FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, config);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/file1FromB' gets synchronized with A.");
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA);

		logger.info("Move file 'file1FromB' at A into shared subfolder 'sharedfolder/subfolder'.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromBAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), fileFromBAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'file1FromB' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveToSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file2FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file2FromB",
				new Random().nextInt(maxNumChunks) + 1, sharedFolderB, config);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/file2FromB' gets synchronized with A.");
		File fileFromBAtA = new File(sharedFolderB, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA);

		logger.info("Move file 'file2FromB' at B into shared subfolder 'sharedfolder/subfolder'.");
		File movedFileFromBAtB = new File(subFolder1AtB, fileFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved file 'fileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(subFolder1AtA, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromBAtA);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeAddSufolderFromAMoveToSubfolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1FromA' from A.");
		File folderFromAAtA = new File(sharedFolderA, "subfolder1FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1FromA' gets synchronized with B.");
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB);

		logger.info("Move folder 'subfolder1FromA' at A into shared subfolder 'sharedfolder/subfolder'.");
		File movedFolderFromAAtA = new File(subFolder1AtA, folderFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subfolder1FromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveToSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder2FromA' from A.");
		File folderFromAAtA = new File(sharedFolderA, "subfolder2FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder2FromA' gets synchronized with B.");
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB);

		logger.info("Move folder 'subfolder2FromA' at B into shared subfolder 'sharedfolder/subfolder'.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(1), folderFromAAtB, movedFolderFromAAtB);

		logger.info("Wait till moved folder 'subfolder2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder1AtA, folderFromAAtB.getName());
		waitTillSynchronized(movedFolderFromAAtB);
		compareFiles(movedFileFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFileFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToSubfolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1FromB' from B.");
		File folderFromBAtB = new File(sharedFolderB, "subfolder1FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1FromB' gets synchronized with A.");
		File folderFromBAtA = new File(sharedFolderA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA);

		logger.info("Move folder 'subfolder1FromB' at A into shared subfolder 'sharedfolder/subfolder'.");
		File movedFolderFromAAtA = new File(subFolder1AtA, folderFromBAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), folderFromBAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subfolder1FromB' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToSubfolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder2FromB' from B.");
		File folderFromBAtB = new File(sharedFolderB, "subfolder2FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder2FromB' gets synchronized with A.");
		File folderFromBAtA = new File(sharedFolderB, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA);

		logger.info("Move folder 'subfolder2FromB' at B into shared subfolder 'sharedfolder/subfolder'.");
		File movedFolderFromBAtB = new File(subFolder1AtB, folderFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till moved folder 'subfolderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(subFolder1AtA, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromBAtA);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromBAtA, movedFolderFromBAtB);
	}

	/**
	 * 
	 * Move
	 * ====
	 * Internal
	 * -------
	 * addSubFileFromAMoveToFolderAtA
	 * addSubFileFromAMoveToFolderAtB
	 * addSubFileFromBMoveToFolderAtA
	 * addSubFileFromBMoveToFolderAtB
	 * 
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

	private static void checkIndex(File oldFileAtA, File oldFileAtB, File newFileAtA, File newFileAtB)
			throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index oldIndexAtA = userProfileA.getFileByPath(rootA.toPath().relativize(oldFileAtA.toPath()));
		Index newIndexAtA = userProfileA.getFileByPath(rootA.toPath().relativize(newFileAtA.toPath()));

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Index oldIndexAtB = userProfileA.getFileByPath(rootB.toPath().relativize(oldFileAtB.toPath()));
		Index newIndexAtB = userProfileB.getFileByPath(rootB.toPath().relativize(newFileAtB.toPath()));

		// check if old indexes have been removed
		Assert.assertNull(oldIndexAtA);
		Assert.assertNull(oldIndexAtB);

		// check if content protection keys are the same
		Assert.assertTrue(newIndexAtA.getProtectionKeys().getPrivate()
				.equals(newIndexAtB.getProtectionKeys().getPrivate()));
		Assert.assertTrue(newIndexAtA.getProtectionKeys().getPublic()
				.equals(newIndexAtB.getProtectionKeys().getPublic()));

		// check if isShared flag is set
		Assert.assertTrue(newIndexAtA.isShared());
		Assert.assertTrue(newIndexAtB.isShared());

		// check write access
		Assert.assertTrue(newIndexAtA.canWrite());
		Assert.assertTrue(newIndexAtB.canWrite());

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
		if (newFileAtA.isDirectory()) {
			Assert.assertTrue(newIndexAtA.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(newIndexAtA.isFile());
		}

		// check user permissions in case of a folder at B
		if (newFileAtB.isDirectory()) {
			Assert.assertTrue(newIndexAtB.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(newIndexAtB.isFile());
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
