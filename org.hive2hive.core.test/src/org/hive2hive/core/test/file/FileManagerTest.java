package org.hive2hive.core.test.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
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

public class FileManagerTest extends H2HJUnitTest {

	private FileManager fileManager;
	private FileTreeNode root;
	private FileTreeNode child1;
	private FileTreeNode child2;
	private FileTreeNode dir1;
	private FileTreeNode child3;
	private FileTreeNode dir2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileManagerTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createFileManager() {
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root);
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(fileManager.getRoot());
	}

	@Before
	public void createTreeNode() {
		// create a tree
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		root = new FileTreeNode(keys);

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
		child1 = new FileTreeNode(root, keys, "1f1", null);
		child2 = new FileTreeNode(root, keys, "1f2", null);
		dir1 = new FileTreeNode(root, keys, "1d");
		child3 = new FileTreeNode(dir1, keys, "2f", null);
		dir2 = new FileTreeNode(dir1, keys, "2d");
	}

	@Test
	public void testMissingOnDisk() throws IOException {
		// create similar structure on disk (1f2 and 2d missing)
		File rootFile = fileManager.getRoot();
		File dir1File = new File(rootFile, "1d");
		FileUtils.writeStringToFile(new File(rootFile, "1f1"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(new File(dir1File, "2f"), NetworkTestUtil.randomString());

		List<FileTreeNode> missingOnDisk = fileManager.getMissingOnDisk(root);
		Assert.assertTrue(missingOnDisk.contains(child2));
		Assert.assertTrue(missingOnDisk.contains(dir2));
		Assert.assertEquals(2, missingOnDisk.size());
	}

	@Test
	public void testMissingInTree() throws IOException {
		// create similar structure on disk (1f2 missing, but 1f3, 2dn and 2fn in addition)
		File rootFile = fileManager.getRoot();
		File dir1File = new File(rootFile, "1d");
		FileUtils.writeStringToFile(new File(rootFile, "1f1"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(new File(rootFile, "1f3"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(new File(dir1File, "2f"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(new File(dir1File, "2fn"), NetworkTestUtil.randomString());
		new File(dir1File, "2dn").mkdir();

		List<File> missingInTree = fileManager.getMissingInTree(root);
		Assert.assertEquals(3, missingInTree.size());
		for (File file : missingInTree) {
			Assert.assertTrue(file.getName().equalsIgnoreCase("1f3")
					|| file.getName().equalsIgnoreCase("2dn") || file.getName().equalsIgnoreCase("2fn"));
		}
	}

	@Test
	public void testChangedFiles() throws IOException {
		// create similar structure on disk (2d missing)
		File rootFile = fileManager.getRoot();
		File dir1File = new File(rootFile, "1d");
		File file1f1 = new File(rootFile, "1f1");
		File file1f2 = new File(rootFile, "1f2");
		File file2f = new File(dir1File, "2f");

		FileUtils.writeStringToFile(file1f1, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file1f2, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file2f, NetworkTestUtil.randomString());

		// set the correct md5 hashes
		child1.setMD5(EncryptionUtil.generateMD5Hash(new FileInputStream(file1f1)));
		child2.setMD5(EncryptionUtil.generateMD5Hash(new FileInputStream(file1f2)));
		child3.setMD5(EncryptionUtil.generateMD5Hash(new FileInputStream(file2f)));

		// no files are changed
		List<FileTreeNode> changed = fileManager.getChangedFiles(root);
		Assert.assertTrue(changed.isEmpty());

		// write other random string to 1f2
		FileUtils.writeStringToFile(file1f2, NetworkTestUtil.randomString());
		// simulate that other user changed hash in profile
		child3.setMD5(EncryptionUtil.generateMD5Hash(NetworkTestUtil.randomString().getBytes()));

		// two files are now changed
		changed = fileManager.getChangedFiles(root);
		Assert.assertEquals(2, changed.size());
		Assert.assertTrue(changed.contains(child2));
		Assert.assertTrue(changed.contains(child3));
	}

	@Test
	public void testCreateFileOnDisk() throws IOException {
		// creation of file works
		Assert.assertTrue(fileManager.createFileOnDisk(child1));

		// does not work because dir1 does not exist yet
		Assert.assertFalse(fileManager.createFileOnDisk(dir2));
		Assert.assertTrue(fileManager.createFileOnDisk(dir1));

		// works now
		Assert.assertTrue(fileManager.createFileOnDisk(dir2));
	}
}
