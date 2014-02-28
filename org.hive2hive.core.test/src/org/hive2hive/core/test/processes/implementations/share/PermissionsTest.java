package org.hive2hive.core.test.processes.implementations.share;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;
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
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.file.FileTestUtil;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test class tests the permission handling for shared folder. <code>Hive2Hive</code> supports currently
 * two kinds of access (see {@link PermissionType}): read-only and write.
 * 
 * @author Seppi
 */
public class PermissionsTest extends H2HJUnitTest {

	private static final IFileConfiguration config = new TestFileConfiguration();
	private static final int networkSize = 3;
	private static List<NetworkManager> network;

	private File rootA;
	private File rootB;
	private UserCredentials userA;
	private UserCredentials userB;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ShareFolderTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
	}

	/**
	 * Setup two users with each one client, log them in
	 * 
	 * @throws NoPeerConnectionException
	 */
	@Before
	public void setup() throws NoSessionException, NoPeerConnectionException {
		rootA = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		userA = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userA, network.get(0), rootA);

		rootB = new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
		userB = NetworkTestUtil.generateRandomCredentials();
		UseCaseTestUtil.registerAndLogin(userB, network.get(1), rootB);
	}

	/**
	 * The largest structure during test:
	 * /root
	 * ../folder1
	 * ..../file1
	 * ..../file2
	 * ..../subfolder1
	 * ......(empty)
	 * ..../subfolder2
	 * ....../file3
	 * ....../file4
	 * 
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalFileLocation
	 * @throws GetFailedException
	 */
	@Test
	public void testShareWithWritePermission() throws NoSessionException, NoPeerConnectionException, IOException,
			IllegalFileLocation, IllegalArgumentException, GetFailedException {
		/**
		 * 1. upload folder "folder1" from A
		 */
		File folder1AtA = new File(rootA, "folder1");
		folder1AtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folder1AtA);

		/**
		 * 2. share folder "folder1" with user B giving write permissions
		 */
		UseCaseTestUtil.shareFolder(network.get(0), folder1AtA, userB.getUserId(), PermissionType.WRITE);
		File folder1AtB = new File(rootB, folder1AtA.getName());
		waitTillSynchronized(folder1AtB, true);

		/**
		 * 3. upload a new file "folder1/file1" from A
		 */
		File file1AtA = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(5), folder1AtA,
				config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);
		File file1AtB = new File(folder1AtB, file1AtA.getName());
		waitTillSynchronized(file1AtB, true);
		Assert.assertEquals(file1AtA.length(), file1AtB.length());

		/**
		 * 4. upload a new file "folder1/file2" from B
		 */
		File file2AtB = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5), folder1AtB,
				config);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2AtB);
		File file2AtA = new File(folder1AtA, file2AtB.getName());
		waitTillSynchronized(file2AtA, true);
		Assert.assertEquals(file2AtB.length(), file2AtA.length());

		/**
		 * 5. upload a sub folder "folder1/subfolder1" from A
		 */
		File subFolder1AtA = new File(folder1AtA, "subfolder1");
		subFolder1AtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1AtA);
		File subfolder1AtB = new File(folder1AtB, subFolder1AtA.getName());
		waitTillSynchronized(subfolder1AtB, true);

		/**
		 * 6. upload a sub folder "folder1/subfolder2" from B
		 */
		File subFolder2AtB = new File(folder1AtB, "subfolder2");
		subFolder2AtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), subFolder2AtB);
		File subFolder2AtA = new File(folder1AtA, subFolder2AtB.getName());
		waitTillSynchronized(subFolder2AtA, true);

		/**
		 * 7. upload a file "folder1/subfolder2/file3" from A
		 */
		File file3AtA = FileTestUtil.createFileRandomContent("file3", new Random().nextInt(5), subFolder2AtA,
				config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file3AtA);
		File file3AtB = new File(subFolder2AtB, file3AtA.getName());
		waitTillSynchronized(file3AtB, true);

		/**
		 * 8. upload a file "folder1/subfolder2/file4" from B
		 */
		File file4AtB = FileTestUtil.createFileRandomContent("file4", new Random().nextInt(5), subFolder2AtB,
				config);
		UseCaseTestUtil.uploadNewFile(network.get(1), file4AtB);
		File file4AtA = new File(subFolder2AtA, file4AtB.getName());
		waitTillSynchronized(file4AtA, true);

		/**
		 * 9. check user A's and user B's {@link UserProfile}
		 */
		UserProfile userProfileA = network.get(0).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		FolderIndex folder1AIndex = (FolderIndex) userProfileA.getFileByPath(Paths.get(folder1AtA.getName()));

		UserProfile userProfileB = network.get(1).getSession().getProfileManager()
				.getUserProfile(UUID.randomUUID().toString(), false);
		FolderIndex folder1BIndex = (FolderIndex) userProfileB.getFileByPath(Paths.get(folder1AtB.getName()));

		// check if content protection keys are the same
		Assert.assertTrue(folder1AIndex.getProtectionKeys().getPrivate()
				.equals(folder1BIndex.getProtectionKeys().getPrivate()));
		Assert.assertTrue(folder1AIndex.getProtectionKeys().getPublic()
				.equals(folder1BIndex.getProtectionKeys().getPublic()));

		// check the folder structure
		checkIndex(folder1AIndex, folder1BIndex.getProtectionKeys());
		checkIndex(folder1BIndex, folder1AIndex.getProtectionKeys());

		/**
		 * 10. delete "file4" at user A
		 */
		UseCaseTestUtil.deleteFile(network.get(0), file4AtA);
		waitTillSynchronized(file4AtB, false);

		/**
		 * 11. delete "subfolder2" at user B
		 */
		UseCaseTestUtil.deleteFile(network.get(1), subFolder2AtB);
		waitTillSynchronized(subFolder2AtB, false);

		/**
		 * 12. move "file2" at user A to root folder
		 */
		UseCaseTestUtil.moveFile(network.get(0), file2AtA, rootA);
		waitTillSynchronized(file2AtB, false);

		/**
		 * 13. move "file1" at user B to "subfolder1"
		 */
		UseCaseTestUtil.moveFile(network.get(1), file1AtB, subfolder1AtB);
		waitTillSynchronized(file1AtA, false);
		file1AtA = new File(subFolder1AtA, "file1");
		waitTillSynchronized(file1AtA, true);

		/**
		 * 14. move "subfolder1" at user A to root folder
		 */
		UseCaseTestUtil.moveFile(network.get(0), subFolder1AtA, rootA);
		waitTillSynchronized(subfolder1AtB, false);

		/**
		 * 15. delete "folder1" at user B
		 */
		UseCaseTestUtil.deleteFile(network.get(1), folder1AtB);
		waitTillSynchronized(folder1AtA, false);

	}

	@Test
	public void testShareWithReadPermission() throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation, IllegalArgumentException, IOException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		/**
		 * 1. upload folder "folder1" from A
		 */
		File folder1AtA = new File(rootA, "folder1");
		folder1AtA.mkdirs();
		UseCaseTestUtil.executeProcessTillSucceded(ProcessFactory.instance().createNewFileProcess(folder1AtA,
				nodeA));

		/**
		 * 2. share folder "folder1" with user B giving only read permissions
		 */
		UseCaseTestUtil.executeProcessTillSucceded(ProcessFactory.instance().createShareProcess(folder1AtA,
				new UserPermission(userB.getUserId(), PermissionType.READ), nodeA));
		File folder1AtB = new File(rootB, folder1AtA.getName());
		waitTillSynchronized(folder1AtB, true);

		/**
		 * 3. upload a new file "folder1/file1" from A
		 */
		File file1AtA = FileTestUtil.createFileRandomContent("file1", new Random().nextInt(5), folder1AtA,
				config);
		UseCaseTestUtil.executeProcessTillSucceded(ProcessFactory.instance().createNewFileProcess(file1AtA,
				nodeA));
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);
		File file1AtB = new File(folder1AtB, file1AtA.getName());
		waitTillSynchronized(file1AtB, true);

		/**
		 * 4. try to upload a new file "folder1/file2" from B
		 */
		File file2AtB = FileTestUtil.createFileRandomContent("file2", new Random().nextInt(5), folder1AtB,
				config);
		UseCaseTestUtil.executeProcessTillFailed(ProcessFactory.instance().createNewFileProcess(file2AtB,
				nodeB));
	}

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

	private static void checkIndex(Index index, KeyPair protectionKeys) {
		if (index instanceof FolderIndex) {
			FolderIndex folderIndex = (FolderIndex) index;

			// check if isShared flag is set
			Assert.assertTrue(folderIndex.isShared());
			// check for correct content protection key
			Assert.assertTrue(folderIndex.getProtectionKeys().getPrivate()
					.equals(protectionKeys.getPrivate()));
			Assert.assertTrue(folderIndex.getProtectionKeys().getPublic().equals(protectionKeys.getPublic()));

			// TODO check user permissions

			// recursively check children
			for (Index child : folderIndex.getChildren())
				checkIndex(child, protectionKeys);
		}
	}

	@After
	public void deleteRoots() throws IOException {
		FileUtils.deleteDirectory(rootA);
		FileUtils.deleteDirectory(rootB);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
