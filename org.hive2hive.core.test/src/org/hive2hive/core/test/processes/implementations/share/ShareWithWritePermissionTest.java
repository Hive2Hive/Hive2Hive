package org.hive2hive.core.test.processes.implementations.share;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
 * This test class tests share/synchronize scenarios between two users with {@link PermissionType#WRITE}
 * permission.
 * 
 * @author Seppi
 */
public class ShareWithWritePermissionTest extends H2HJUnitTest {

	private static final IFileConfiguration config = new TestFileConfiguration();
	private static final int networkSize = 3;
	private static List<NetworkManager> network;

	private static File rootA;
	private static File rootB;
	private static File sharedFolderA;
	private static File sharedFolderB;

	private static UserCredentials userA;
	private static UserCredentials userB;

	/**
	 * Setup network. Setup two users with each one client, log them in.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ShareWithWritePermissionTest.class;
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

		logger.info("Upload folder 'folder1' from A.");
		sharedFolderA = new File(rootA, "folder1");
		sharedFolderA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderA);

		logger.debug("Share folder 'folder1' with user B giving write permission.");
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderA, userB.getUserId(), PermissionType.WRITE);
		sharedFolderB = new File(rootB, sharedFolderA.getName());
		waitTillSynchronized(sharedFolderB, true);
	}

	@Test
	public void testSynchronizeAddFileFromADeleteFromA() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file1' from A.");
		File file1AtA = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(2) + 1,
				sharedFolderA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);

		logger.info("Wait till new file 'folder1/file1' gets synchronized with B.");
		File file1AtB = new File(sharedFolderB, file1AtA.getName());
		waitTillSynchronized(file1AtB, true);
		compareFiles(file1AtA, file1AtB);
		checkIndex(file1AtA, file1AtB, false);

		logger.info("Delete file 'folder1/file1' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), file1AtA);
		
		logger.info("Wait till deletion of file 'folder1/file2' gets synchronized with B.");
		waitTillSynchronized(file1AtB, false);
		checkIndex(file1AtA, file1AtB, true);
	}
	
	@Test
	public void testSynchronizeAddFileFromADeleteFromB() throws NoSessionException,
			NoPeerConnectionException, IOException, IllegalFileLocation, IllegalArgumentException,
			GetFailedException {
		logger.info("Upload a new file 'folder1/file1' from A.");
		File file1AtA = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(2) + 1,
				sharedFolderA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);

		logger.info("Wait till new file 'folder1/file1' gets synchronized with B.");
		File file1AtB = new File(sharedFolderB, file1AtA.getName());
		waitTillSynchronized(file1AtB, true);
		compareFiles(file1AtA, file1AtB);
		checkIndex(file1AtA, file1AtB, false);

		logger.info("Delete file 'folder1/file1' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), file1AtB);
		
		logger.info("Wait till deletion of file 'folder1/file2' gets synchronized with A.");
		waitTillSynchronized(file1AtA, false);
		checkIndex(file1AtA, file1AtB, true);
	}

	@Test
	public void testSynchronizeAddFileFromBDeleteFromB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'folder1/file2' from B.");
		File file2AtB = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5) + 1,
				sharedFolderB, config);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2AtB);

		logger.info("Wait till new file 'folder1/file2' gets synchronized with A.");
		File file2AtA = new File(sharedFolderA, file2AtB.getName());
		waitTillSynchronized(file2AtA, true);
		compareFiles(file2AtA, file2AtB);
		checkIndex(file2AtA, file2AtB, false);
		
		logger.info("Delete file 'folder1/file2' from B.");
		UseCaseTestUtil.deleteFile(network.get(1), file2AtB);
		
		logger.info("Wait till deletion of file 'folder1/file2' gets synchronized with A.");
		waitTillSynchronized(file2AtA, false);
		checkIndex(file2AtA, file2AtB, true);
	}
	
	@Test
	public void testSynchronizeAddFileFromBDeleteFromA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'folder1/file2' from B.");
		File file2AtB = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5) + 1,
				sharedFolderB, config);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2AtB);

		logger.info("Wait till new file 'folder1/file2' gets synchronized with A.");
		File file2AtA = new File(sharedFolderA, file2AtB.getName());
		waitTillSynchronized(file2AtA, true);
		compareFiles(file2AtA, file2AtB);
		checkIndex(file2AtA, file2AtB, false);
		
		logger.info("Delete file 'folder1/file2' from A.");
		UseCaseTestUtil.deleteFile(network.get(0), file2AtA);
		
		logger.info("Wait till deletion of file 'folder1/file2' gets synchronized with B.");
		waitTillSynchronized(file2AtB, false);
		checkIndex(file2AtA, file2AtB, true);
	}

	//
	// @Test
	// public void testShareWithWritePermission() throws NoSessionException, NoPeerConnectionException,
	// IOException, IllegalFileLocation, IllegalArgumentException, GetFailedException {
	//
	// logger.debug("5. Upload a sub folder 'folder1/subfolder1' from A.");
	// File subFolder1AtA = new File(folder1AtA, "subfolder1");
	// subFolder1AtA.mkdir();
	// UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1AtA);
	// File subfolder1AtB = new File(folder1AtB, subFolder1AtA.getName());
	// waitTillSynchronized(subfolder1AtB, true);
	//
	// logger.debug("6. Upload a sub folder 'folder1/subfolder2' from B.");
	// File subFolder2AtB = new File(folder1AtB, "subfolder2");
	// subFolder2AtB.mkdir();
	// UseCaseTestUtil.uploadNewFile(network.get(1), subFolder2AtB);
	// File subFolder2AtA = new File(folder1AtA, subFolder2AtB.getName());
	// waitTillSynchronized(subFolder2AtA, true);
	//
	// logger.debug("7. Upload a file 'folder1/subfolder2/file3' from A.");
	// File file3AtA = FileTestUtil.createFileRandomContent("file3", new Random().nextInt(5) + 1,
	// subFolder2AtA, config);
	// UseCaseTestUtil.uploadNewFile(network.get(0), file3AtA);
	// File file3AtB = new File(subFolder2AtB, file3AtA.getName());
	// waitTillSynchronized(file3AtB, true);
	//
	// logger.debug("8. Upload a file 'folder1/subfolder2/file4' from B.");
	// File file4AtB = FileTestUtil.createFileRandomContent("file4", new Random().nextInt(5) + 1,
	// subFolder2AtB, config);
	// UseCaseTestUtil.uploadNewFile(network.get(1), file4AtB);
	// File file4AtA = new File(subFolder2AtA, file4AtB.getName());
	// waitTillSynchronized(file4AtA, true);
	//
	// logger.debug("9. Check user A's and user B's user profile.");
	// UserProfile userProfileA = network.get(0).getSession().getProfileManager()
	// .getUserProfile(UUID.randomUUID().toString(), false);
	// FolderIndex folder1AIndex = (FolderIndex) userProfileA.getFileByPath(Paths.get(folder1AtA.getName()));
	//
	// UserProfile userProfileB = network.get(1).getSession().getProfileManager()
	// .getUserProfile(UUID.randomUUID().toString(), false);
	// FolderIndex folder1BIndex = (FolderIndex) userProfileB.getFileByPath(Paths.get(folder1AtB.getName()));
	//
	// // check if content protection keys are the same
	// Assert.assertTrue(folder1AIndex.getProtectionKeys().getPrivate()
	// .equals(folder1BIndex.getProtectionKeys().getPrivate()));
	// Assert.assertTrue(folder1AIndex.getProtectionKeys().getPublic()
	// .equals(folder1BIndex.getProtectionKeys().getPublic()));
	//
	// // check the folder structure
	// checkIndex(folder1AIndex, folder1BIndex.getProtectionKeys());
	// checkIndex(folder1BIndex, folder1AIndex.getProtectionKeys());
	//
	// logger.debug("10. Delete 'file3' at user A.");
	// UseCaseTestUtil.deleteFile(network.get(0), file3AtA);
	// waitTillSynchronized(file3AtB, false);
	//
	// logger.debug("11. Delete 'file4' at user B.");
	// UseCaseTestUtil.deleteFile(network.get(1), file4AtB);
	// waitTillSynchronized(file4AtA, false);
	//
	// logger.debug("12. Delete 'subfolder2' at user B.");
	// UseCaseTestUtil.deleteFile(network.get(1), subFolder2AtB);
	// waitTillSynchronized(subFolder2AtB, false);
	//
	// logger.debug("13. Move 'file2' at user A to root folder.");
	// UseCaseTestUtil.moveFile(network.get(0), file2AtA, new File(rootA, file2AtA.getName()));
	// waitTillSynchronized(file2AtB, false);
	//
	// logger.debug("14. Move 'file1' at user B to 'folder1/subfolder1'.");
	// UseCaseTestUtil.moveFile(network.get(1), file1AtB, new File(subfolder1AtB, file1AtB.getName()));
	// waitTillSynchronized(file1AtA, false);
	// file1AtA = new File(subFolder1AtA, "file1");
	// waitTillSynchronized(file1AtA, true);
	//
	// logger.debug("15. Move 'folder1/subfolder1' at user A to root folder.");
	// UseCaseTestUtil.moveFile(network.get(0), subFolder1AtA, new File(rootA, subFolder1AtA.getName()));
	// waitTillSynchronized(subfolder1AtB, false);
	//
	// logger.debug("16. Delete 'folder1' at user B.");
	// UseCaseTestUtil.deleteFile(network.get(1), folder1AtB);
	// waitTillSynchronized(folder1AtA, false);
	// }

	/*
	 * TODO a further test is missing where the sharing partner (with read permissions) verifies the commands
	 * sent by the other partner. This includes, add, update, move and delete commands.
	 */

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
		Assert.assertEquals(originalFile.length(), synchronizedFile.length());
		Assert.assertEquals(FileUtils.readFileToString(originalFile),
				FileUtils.readFileToString(synchronizedFile));
	}

	private static void checkIndex(File fileAtA, File fileAtB, boolean deleted) throws GetFailedException, NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathA = rootA.toPath().relativize(fileAtA.toPath());
		Index indexA = userProfileA.getFileByPath(relativePathA);

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		Path relativePathB = rootA.toPath().relativize(fileAtA.toPath());
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
