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
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests moving files and folder into a
 * shared folder.
 * 
 * @author Seppi
 */
public class SharedFolderWithWritePermissionMoveInTest extends H2HJUnitTest {

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
		testClass = SharedFolderWithWritePermissionMoveInTest.class;
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

		logger.info("Upload folder 'sharedfolder' from A.");
		sharedFolderA = new File(rootA, "sharedfolder");
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderA);

		logger.info("Share folder 'sharedfolder' with user B giving write permission.");
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.WRITE);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder'.");
		subFolderA = new File(sharedFolderA, "subfolder");
		subFolderA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolderA);
		subFolderB = new File(sharedFolderB, subFolderA.getName());
		waitTillSynchronized(subFolderB);
	}

	@Test
	public void testSynchronizeMoveFileFromAIntoSharedFolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'fileFromA' into root folder of A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("fileFromA",
				new Random().nextInt(maxNumChunks) + 1, rootA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Move file 'fileFromA' at A into shared folder 'sharedfolder'.");
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file 'fileFromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromBIntoSharedFolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'fileFromB' into root folder of B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("fileFromB",
				new Random().nextInt(maxNumChunks) + 1, rootB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Move file 'fileFromB' at B into shared folder 'sharedfolder'.");
		File movedFileFromBAtB = new File(sharedFolderB, fileFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved in file 'fileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(movedFileFromBAtA);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromAIntoSharedFolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'folderFromA' into root folder of A.");
		File folderFromAAtA = new File(rootA, "folderFromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Move folder 'folderFromA' at A into shared folder 'sharedfolder'.");
		File movedFolderFromAAtA = new File(sharedFolderA, folderFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder 'folderFromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromBIntoSharedFolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'folderFromB' into root folder of B.");
		File folderFromBAtB = new File(rootB, "folderFromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Move folder 'folderFromB' at B into shared folder 'sharedfolder'.");
		File movedFolderFromBAtB = new File(sharedFolderB, folderFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till new moved in folder 'folderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(sharedFolderA, folderFromBAtB.getName());
		waitTillSynchronized(movedFolderFromBAtA);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(movedFolderFromBAtA, movedFolderFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromAIntoSharedSubFolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'subfileFromA' into root folder of A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfileFromA",
				new Random().nextInt(maxNumChunks) + 1, rootA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Move file 'subfileFromA' at A into shared folder 'sharedfolder/subfolderA'.");
		File movedFileFromAAtA = new File(subFolderA, fileFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved in file 'subfileFromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFileFromBIntoSharedSubFolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'subfileFromB' into root folder of B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfileFromB",
				new Random().nextInt(maxNumChunks) + 1, rootB, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Move file 'subfileFromB' at B into shared folder 'sharedfolder/subfolder'.");
		File movedFileFromBAtB = new File(subFolderB, fileFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved in file 'subfileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(subFolderA, fileFromBAtB.getName());
		waitTillSynchronized(movedFileFromBAtA);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromAIntoSharedSubFolderAtA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'subfolderFromA' into root folder of A.");
		File folderFromAAtA = new File(rootA, "subfolderFromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Move folder 'subfolderFromA' at A into shared folder 'sharedfolder/subfolder'.");
		File movedFolderFromAAtA = new File(subFolderA, folderFromAAtA.getName());
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till new moved in folder 'subfolderFromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeMoveFolderFromBIntoSharedSubFolderAtB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new folder 'subfolderFromB' into root folder of B.");
		File folderFromBAtB = new File(rootB, "subfolderFromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Move folder 'subfolderFromB' at B into shared folder 'sharedFolder/subfolder'.");
		File movedFolderFromBAtB = new File(subFolderB, folderFromBAtB.getName());
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till new moved in folder 'subfolderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(subFolderA, folderFromBAtB.getName());
		waitTillSynchronized(movedFolderFromBAtA);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(movedFolderFromBAtA, movedFolderFromBAtB);
	}

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

	private static void checkIndex(File fileAtA, File fileAtB) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathA = rootA.toPath().relativize(fileAtA.toPath());
		Index indexA = userProfileA.getFileByPath(relativePathA);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathB = rootB.toPath().relativize(fileAtB.toPath());
		Index indexB = userProfileB.getFileByPath(relativePathB);

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
