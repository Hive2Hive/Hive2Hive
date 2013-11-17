package org.hive2hive.core.test.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileSynchronizerTest extends H2HJUnitTest {

	private FileManager fileManager;
	private FileTreeNode root;
	private FileTreeNode node1f1;
	private FileTreeNode node1f2;
	private FileTreeNode node1d;
	private FileTreeNode node2f;
	private FileTreeNode node2d;
	private File fileRoot;
	private File file1f1;
	private File file1f2;
	private File file1d;
	private File file2f;
	private File file2d;
	private UserProfile userProfile;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileSynchronizerTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createTreeNode() throws IOException {
		String randomName = NetworkTestUtil.randomString();
		fileRoot = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(fileRoot);

		// naming convention:
		// [number][type][index] where number is the level and type is either 'f' for file or 'd' for
		// directory. The index is to distinct two files/folders on the same level

		// setup is like
		// root:
		// - 1f1
		// - 1f2
		// - 1d:
		// - - 2f
		// - - 2d (empty folder)
		file1f1 = new File(fileRoot, "1f1");
		FileUtils.writeStringToFile(file1f1, NetworkTestUtil.randomString());
		file1f2 = new File(fileRoot, "1f2");
		FileUtils.writeStringToFile(file1f2, NetworkTestUtil.randomString());
		file1d = new File(fileRoot, "1d");
		file1d.mkdirs();
		file2f = new File(file1d, "2f");
		FileUtils.writeStringToFile(file2f, NetworkTestUtil.randomString());
		file2d = new File(file1d, "2d");
		file2d.mkdir();

		KeyPair keys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		userProfile = new UserProfile("test-user", keys, keys);
		root = userProfile.getRoot();
		node1f1 = new FileTreeNode(root, keys, "1f1", EncryptionUtil.generateMD5Hash(file1f1));
		node1f2 = new FileTreeNode(root, keys, "1f2", EncryptionUtil.generateMD5Hash(file1f2));
		node1d = new FileTreeNode(root, keys, "1d");
		node2f = new FileTreeNode(node1d, keys, "2f", EncryptionUtil.generateMD5Hash(file2f));
		node2d = new FileTreeNode(node1d, keys, "2d");

		// write the meta data now. Before creating the synchronizer, first modify the file system as desired.
		fileManager.writePersistentMetaData();
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(fileManager.getRoot());
	}

	@Test
	public void testDeletedLocally() throws IOException {
		Files.delete(file1f1.toPath());
		Files.delete(file2d.toPath());

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<FileTreeNode> deletedLocally = fileSynchronizer.getDeletedLocally();
		Assert.assertEquals(2, deletedLocally.size());
		Assert.assertTrue(deletedLocally.contains(node1f1));
		Assert.assertTrue(deletedLocally.contains(node2d));
	}

	@Test
	public void testDeletedRemotely() throws IOException {
		root.removeChild(node1f1);
		root.removeChild(node1d); // delete whole directory

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<File> deletedRemotely = fileSynchronizer.getDeletedRemotely();
		Assert.assertEquals(4, deletedRemotely.size());
		Assert.assertTrue(deletedRemotely.contains(file1f1));
		Assert.assertTrue(deletedRemotely.contains(file1d));
		Assert.assertTrue(deletedRemotely.contains(file2f));
		Assert.assertTrue(deletedRemotely.contains(file2d));
	}

	@Test
	public void testAddedLocally() throws IOException {
		// one folder
		File file2d2 = new File(file1d, "2d2");
		file2d2.mkdir();

		// one file
		File file1f3 = new File(fileRoot, "1f3");
		FileUtils.writeStringToFile(file1f3, NetworkTestUtil.randomString());

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<File> addedLocally = fileSynchronizer.getAddedLocally();
		Assert.assertEquals(2, addedLocally.size());
		Assert.assertTrue(addedLocally.contains(file2d2));
		Assert.assertTrue(addedLocally.contains(file1f3));
	}

	@Test
	public void testAddedRemotely() throws IOException {
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		FileTreeNode node1f3 = new FileTreeNode(root, keys, "1f3", null);
		FileTreeNode node2d2 = new FileTreeNode(node1d, keys, "2d2");

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<FileTreeNode> addedRemotely = fileSynchronizer.getAddedRemotely();
		Assert.assertEquals(2, addedRemotely.size());
		Assert.assertTrue(addedRemotely.contains(node1f3));
		Assert.assertTrue(addedRemotely.contains(node2d2));
	}

	@Test
	public void testUpdatedLocally() throws IOException {
		// change two files
		FileUtils.writeStringToFile(file1f2, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file2f, NetworkTestUtil.randomString());

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<File> updatedLocally = fileSynchronizer.getUpdatedLocally();
		Assert.assertEquals(2, updatedLocally.size());
		Assert.assertTrue(updatedLocally.contains(file1f2));
		Assert.assertTrue(updatedLocally.contains(file2f));

		// change file in user profile as well --> should not occur as updated locally
		node1f2.setMD5(EncryptionUtil.generateMD5Hash(NetworkTestUtil.randomString().getBytes()));

		fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		updatedLocally = fileSynchronizer.getUpdatedLocally();
		Assert.assertEquals(1, updatedLocally.size());
		Assert.assertTrue(updatedLocally.contains(file2f));
	}

	@Test
	public void testUpdatedRemotely() throws IOException {
		// change two files in the user profile; hashes on disk remain the same
		node1f2.setMD5(EncryptionUtil.generateMD5Hash(NetworkTestUtil.randomString().getBytes()));
		node2f.setMD5(EncryptionUtil.generateMD5Hash(NetworkTestUtil.randomString().getBytes()));

		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		List<FileTreeNode> updatedRemotely = fileSynchronizer.getUpdatedRemotely();
		Assert.assertEquals(2, updatedRemotely.size());
		Assert.assertTrue(updatedRemotely.contains(node1f2));
		Assert.assertTrue(updatedRemotely.contains(node2f));

		// change file on disk as well --> should occur as updated remotely since there is a conflict and the
		// profile wins
		FileUtils.writeStringToFile(file1f2, NetworkTestUtil.randomString());

		fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		updatedRemotely = fileSynchronizer.getUpdatedRemotely();
		Assert.assertEquals(2, updatedRemotely.size());
		Assert.assertTrue(updatedRemotely.contains(node2f));
		Assert.assertTrue(updatedRemotely.contains(node1f2));
	}

	@Test
	public void testNothingChanged() {
		// nothing has changed --> should receive no file to upload/download
		FileSynchronizer fileSynchronizer = new FileSynchronizer(fileManager, userProfile);
		Assert.assertEquals(0, fileSynchronizer.getUpdatedRemotely().size());
		Assert.assertEquals(0, fileSynchronizer.getUpdatedLocally().size());
		Assert.assertEquals(0, fileSynchronizer.getAddedRemotely().size());
		Assert.assertEquals(0, fileSynchronizer.getAddedLocally().size());
		Assert.assertEquals(0, fileSynchronizer.getDeletedRemotely().size());
		Assert.assertEquals(0, fileSynchronizer.getDeletedLocally().size());
	}
}
