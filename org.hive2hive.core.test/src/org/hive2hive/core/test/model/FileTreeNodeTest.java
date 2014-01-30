package org.hive2hive.core.test.model;

import java.nio.file.Paths;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the file tree node
 * 
 * @author Nico, Seppi
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
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);
		KeyPair domainKey = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);
		root = new FileTreeNode(keys, domainKey);

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
	public void testFullPath() {
		Assert.assertEquals("", root.getFullPath().toString());
		Assert.assertEquals("1f1", child1.getFullPath().toString());
		Assert.assertEquals("1f2", child2.getFullPath().toString());
		Assert.assertEquals("1d", dir1.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "2f").toString(), child3.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "2d").toString(), dir2.getFullPath().toString());
	}

	@Test
	public void testShared1() {
		// set 1d to be shared
		dir1.setIsShared(true);

		// 1d, 2f and 2d should return to be shared, others not
		Assert.assertTrue(dir1.isShared());
		Assert.assertTrue(dir2.isShared());
		Assert.assertTrue(child3.isShared());

		Assert.assertFalse(root.isShared());
		Assert.assertFalse(child1.isShared());
		Assert.assertFalse(child2.isShared());
		
		// set 1d to be not shared
		dir1.setIsShared(false);
		
		// root, 1f1, 1f2, 1d, 2f and 2d should return to be not shared
		Assert.assertFalse(root.isShared());
		Assert.assertFalse(dir1.isShared());
		Assert.assertFalse(dir2.isShared());
		Assert.assertFalse(child1.isShared());
		Assert.assertFalse(child2.isShared());
		Assert.assertFalse(child3.isShared());

	}

	@Test
	public void testShared2() {
		try {
			root.setIsShared(true);
			Assert.fail();
		} catch (IllegalStateException e) {
			// has to be thrown
		}
	}

	@Test
	public void testHasShared() {
		// set 2d to be shared
		dir2.setIsShared(true);

		// root, 1d and 2d should show that they contain a shared folder
		Assert.assertTrue(root.isSharedOrHasSharedChildren());
		Assert.assertTrue(dir1.isSharedOrHasSharedChildren());
		Assert.assertTrue(dir2.isSharedOrHasSharedChildren());

		Assert.assertFalse(child1.isSharedOrHasSharedChildren());
		Assert.assertFalse(child2.isSharedOrHasSharedChildren());
		Assert.assertFalse(child3.isSharedOrHasSharedChildren());
		
		// set 2d to be not shared
		dir2.setIsShared(false);

		// root, 1f1, 1f2, 1d, 2f and 2d should not contain a shared folder
		Assert.assertFalse(root.isSharedOrHasSharedChildren());
		Assert.assertFalse(dir1.isSharedOrHasSharedChildren());
		Assert.assertFalse(dir2.isSharedOrHasSharedChildren());
		Assert.assertFalse(child1.isSharedOrHasSharedChildren());
		Assert.assertFalse(child2.isSharedOrHasSharedChildren());
		Assert.assertFalse(child3.isSharedOrHasSharedChildren());
		
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
