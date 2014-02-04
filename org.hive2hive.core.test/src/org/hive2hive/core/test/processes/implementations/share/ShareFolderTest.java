package org.hive2hive.core.test.processes.implementations.share;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
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
 * Test the share function. A folder can be shared among multiple users.
 * 
 * @author Nico
 * 
 */
// TODO Test share with more than 1 user
public class ShareFolderTest extends H2HJUnitTest {

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

	@Test
	public void shareEmptyFolderTest() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File folderToShare = new File(rootA, "folder1");
		folderToShare.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderToShare);

		UseCaseTestUtil.shareFolder(network.get(0), folderToShare, userB.getUserId());

		// TODO wait for userB to process the user profile task
		Thread.sleep(10000);

		File folderAtB = new File(rootB, folderToShare.getName());
		Assert.assertTrue(folderAtB.exists());
	}

	@Test
	public void shareFilledFolderTest() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File folderToShare = new File(rootA, "folder1");
		folderToShare.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderToShare);

		File file1 = FileTestUtil.createFileRandomContent(new Random().nextInt(5), folderToShare, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1);
		File file2 = FileTestUtil.createFileRandomContent(new Random().nextInt(5), folderToShare, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file2);
		File file3 = FileTestUtil.createFileRandomContent(new Random().nextInt(5), folderToShare, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file3);
		File subfolder = new File(folderToShare, "subfolder");
		subfolder.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subfolder);

		// share the filled folder
		UseCaseTestUtil.shareFolder(network.get(0), folderToShare, userB.getUserId());

		// TODO wait for userB to process the user profile task
		Thread.sleep(20000);

		// check the files and the folders at user B
		File sharedFolderAtB = new File(rootB, folderToShare.getName());
		Assert.assertTrue(sharedFolderAtB.exists());

		File file1AtB = new File(sharedFolderAtB, file1.getName());
		Assert.assertTrue(file1AtB.exists());
		Assert.assertEquals(file1.length(), file1AtB.length());

		File file2AtB = new File(sharedFolderAtB, file2.getName());
		Assert.assertTrue(file2AtB.exists());
		Assert.assertEquals(file2.length(), file2AtB.length());

		File file3AtB = new File(sharedFolderAtB, file3.getName());
		Assert.assertTrue(file3AtB.exists());
		Assert.assertEquals(file3.length(), file3AtB.length());

		File subfolderAtB = new File(sharedFolderAtB, subfolder.getName());
		Assert.assertTrue(subfolderAtB.exists());
	}

	@Test
	public void shareFolderFillAfterwardsTest() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File sharedFolderAtA = new File(rootA, "folder1");
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		// share the empty folder
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId());

		// TODO wait for userB to process the user profile task
		Thread.sleep(10000);

		// check the folder at user B
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		Assert.assertTrue(sharedFolderAtB.exists());

		// upload a new file at A
		File file1AtA = FileTestUtil
				.createFileRandomContent(new Random().nextInt(5), sharedFolderAtA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);

		// TODO wait for userB to receive the file
		Thread.sleep(10000);

		// verify if user B got the file too
		File file1AtB = new File(sharedFolderAtB, file1AtA.getName());
		Assert.assertTrue(file1AtB.exists());
		Assert.assertEquals(file1AtA.length(), file1AtB.length());

		// upload a new file at B
		File file2AtB = FileTestUtil
				.createFileRandomContent(new Random().nextInt(5), sharedFolderAtB, config);
		UseCaseTestUtil.uploadNewFile(network.get(1), file2AtB);

		// TODO wait for userA to receive the file
		Thread.sleep(10000);

		File file2AtA = new File(sharedFolderAtA, file2AtB.getName());
		Assert.assertTrue(file2AtA.exists());
		Assert.assertEquals(file2AtB.length(), file2AtA.length());
	}

	@Test
	public void shareFolderDeleteFile() throws IOException, IllegalFileLocation, NoSessionException,
			GetFailedException, InterruptedException, NoPeerConnectionException {
		// upload an empty folder
		File sharedFolderAtA = new File(rootA, "folder1");
		sharedFolderAtA.mkdirs();
		UseCaseTestUtil.uploadNewFile(network.get(0), sharedFolderAtA);

		File file1AtA = FileTestUtil
				.createFileRandomContent(new Random().nextInt(5), sharedFolderAtA, config);
		UseCaseTestUtil.uploadNewFile(network.get(0), file1AtA);

		// share the empty folder
		UseCaseTestUtil.shareFolder(network.get(0), sharedFolderAtA, userB.getUserId());

		// TODO wait for userB to process the user profile task
		Thread.sleep(10000);

		// check the folder and the file at user B
		File sharedFolderAtB = new File(rootB, sharedFolderAtA.getName());
		File file1AtB = new File(sharedFolderAtB, file1AtA.getName());
		Assert.assertTrue(sharedFolderAtB.exists());
		Assert.assertTrue(file1AtB.exists());
		Assert.assertEquals(file1AtA.length(), file1AtB.length());

		// delete the file at B
		UseCaseTestUtil.deleteFile(network.get(1), file1AtB);

		// TODO wait for user A to receive the file deletion
		Thread.sleep(10000);

		// verify that the file has been deleted at A and B
		Assert.assertFalse(file1AtB.exists());
		Assert.assertFalse(file1AtA.exists());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shareFileTest() throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		File file = new File(rootA, NetworkTestUtil.randomString());
		UseCaseTestUtil.shareFolder(network.get(0), file, "any");
	}

	@Test(expected = IllegalFileLocation.class)
	public void wrongFolderLocationTest() throws IllegalFileLocation, IllegalArgumentException,
			NoSessionException, NoPeerConnectionException {
		// share root of B through client A
		UseCaseTestUtil.shareFolder(network.get(0), rootB, "any");
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
