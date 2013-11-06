package org.hive2hive.core.test.file;

import java.io.File;
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

	@Test
	public void testMissingOnDisk() throws IOException {
		// create a tree
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		FileTreeNode rootNode = new FileTreeNode(keys);

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
		FileTreeNode child1 = new FileTreeNode(rootNode, keys, "1f1", false);
		FileTreeNode child2 = new FileTreeNode(rootNode, keys, "1f2", false);
		FileTreeNode dir1 = new FileTreeNode(rootNode, keys, "1d", true);
		FileTreeNode child3 = new FileTreeNode(dir1, keys, "2f", false);
		FileTreeNode dir2 = new FileTreeNode(dir1, keys, "2d", true);

		// create similar structure on disk (1f2 and 2d missing)
		File root = fileManager.getRoot();
		File dir1File = new File(root, "1d");
		FileUtils.writeStringToFile(new File(root, "1f1"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(new File(dir1File, "2f"), NetworkTestUtil.randomString());

		List<FileTreeNode> missingOnDisk = fileManager.getMissingOnDisk(rootNode);
		Assert.assertTrue(missingOnDisk.contains(child2));
		Assert.assertTrue(missingOnDisk.contains(dir2));
		Assert.assertEquals(2, missingOnDisk.size());
	}
}
