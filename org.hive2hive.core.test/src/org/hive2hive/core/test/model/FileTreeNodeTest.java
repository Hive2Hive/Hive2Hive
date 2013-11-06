package org.hive2hive.core.test.model;

import java.security.KeyPair;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the file tree node
 * 
 * @author Nico
 * 
 */
public class FileTreeNodeTest extends H2HJUnitTest {

	private FileTreeNode root;
	private FileTreeNode child1;
	private FileTreeNode child2;
	private FileTreeNode dir1;
	private FileTreeNode child3;
	private FileTreeNode dir2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileTreeNodeTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
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
		child1 = new FileTreeNode(root, keys, "1f1", false);
		child2 = new FileTreeNode(root, keys, "1f2", false);
		dir1 = new FileTreeNode(root, keys, "1d", true);
		child3 = new FileTreeNode(dir1, keys, "2f", false);
		dir2 = new FileTreeNode(dir1, keys, "2d", true);
	}

	@Test
	public void testFullPath() {
		Assert.assertEquals(FileManager.FILE_SEP, root.getFullPath());
		Assert.assertEquals(FileManager.FILE_SEP + "1f1", child1.getFullPath());
		Assert.assertEquals(FileManager.FILE_SEP + "1f2", child2.getFullPath());
		Assert.assertEquals(FileManager.FILE_SEP + "1d" + FileManager.FILE_SEP, dir1.getFullPath());
		Assert.assertEquals(FileManager.FILE_SEP + "1d" + FileManager.FILE_SEP + "2f", child3.getFullPath());
		Assert.assertEquals(FileManager.FILE_SEP + "1d" + FileManager.FILE_SEP + "2d" + FileManager.FILE_SEP,
				dir2.getFullPath());
	}

	@Test
	public void testShared() {
		// set 1d to be shared
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		dir1.setDomainKeys(keys);

		// 1d, 2f and 2d should return to be shared, others not
		Assert.assertTrue(dir1.isShared());
		Assert.assertTrue(dir2.isShared());
		Assert.assertTrue(child3.isShared());

		Assert.assertFalse(root.isShared());
		Assert.assertFalse(child1.isShared());
		Assert.assertFalse(child2.isShared());
	}

	@Test
	public void testGetChildByName() {
		Assert.assertEquals(child1, root.getChildByName("1f1"));
		Assert.assertEquals(dir1, root.getChildByName("1d"));
		Assert.assertEquals(null, root.getChildByName("2f"));
		Assert.assertEquals(null, root.getChildByName(null));
		Assert.assertEquals(null, root.getChildByName(""));

	}
}
