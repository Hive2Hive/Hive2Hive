package org.hive2hive.core.model;

import java.nio.file.Paths;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.security.EncryptionUtil;
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
public class IndexTest extends H2HJUnitTest {

	private final String userId = "UserA";
	private FolderIndex root;
	private Index child1;
	private Index child2;
	private FolderIndex dir1;
	private Index child3;
	private FolderIndex dir2;
	private FolderIndex dir3;
	private FolderIndex dir4;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = IndexTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createTreeNode() {
		// create a tree
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		KeyPair protectionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		root = new FolderIndex(null, keys, null);
		root.addUserPermissions(new UserPermission(userId, PermissionType.WRITE));
		root.setProtectionKeys(protectionKeys);

		// naming convention:
		// [number][type][index] where number is the level and type is either 'f' for file or 'd' for
		// directory. The index is to distinct two files/folders on the same level

		// setup is like
		// root:
		// - child1
		// - child2
		// - 1d:
		// - - child3
		// - - 2d (empty folder)
		// - - 3d:
		// - - - 4d (empty folder)
		child1 = new FileIndex(root, keys, "1f1", null);
		child2 = new FileIndex(root, keys, "1f2", null);
		dir1 = new FolderIndex(root, keys, "1d");
		child3 = new FileIndex(dir1, keys, "2f", null);
		dir2 = new FolderIndex(dir1, keys, "2d");
		dir3 = new FolderIndex(dir1, keys, "3d");
		dir4 = new FolderIndex(dir3, keys, "4d");
	}

	@Test
	public void testFullPath() {
		Assert.assertEquals("", root.getFullPath().toString());
		Assert.assertEquals("1f1", child1.getFullPath().toString());
		Assert.assertEquals("1f2", child2.getFullPath().toString());
		Assert.assertEquals("1d", dir1.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "2f").toString(), child3.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "2d").toString(), dir2.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "3d").toString(), dir3.getFullPath().toString());
		Assert.assertEquals(Paths.get("1d", "3d", "4d").toString(), dir4.getFullPath().toString());
	}

	@Test
	public void testShare() {
		// set 1d to be shared (use a shorter key to speed up)
		dir1.share(EncryptionUtil.generateRSAKeyPair(EncryptionUtil.RSA_KEYLENGTH.BIT_512));

		// 1d, 2f, 2d, 3d and 4d should return to be shared, others not
		Assert.assertTrue(dir1.isShared());
		Assert.assertTrue(dir2.isShared());
		Assert.assertTrue(child3.isShared());
		Assert.assertTrue(dir3.isShared());
		Assert.assertTrue(dir4.isShared());
		
		Assert.assertFalse(root.isShared());
		Assert.assertFalse(child1.isShared());
		Assert.assertFalse(child2.isShared());

		// set 1d to be not shared
		dir1.unshare();

		// root, 1f1, 1f2, 1d, 2f and 2d should return to be not shared
		Assert.assertFalse(root.isShared());
		Assert.assertFalse(dir1.isShared());
		Assert.assertFalse(dir2.isShared());
		Assert.assertFalse(dir3.isShared());
		Assert.assertFalse(dir4.isShared());
		Assert.assertFalse(child1.isShared());
		Assert.assertFalse(child2.isShared());
		Assert.assertFalse(child3.isShared());

	}

	@Test(expected = IllegalStateException.class)
	public void testShareRoot() {
		// (use a shorter key to speed up)
		root.share(EncryptionUtil.generateRSAKeyPair(EncryptionUtil.RSA_KEYLENGTH.BIT_512));
	}

	@Test
	public void testHasShared() {
		// set 2d to be shared (use a shorter key to speed up)
		dir2.share(EncryptionUtil.generateRSAKeyPair(EncryptionUtil.RSA_KEYLENGTH.BIT_512));

		// root, 1d and 2d should show that they contain a shared folder
		Assert.assertTrue(root.isSharedOrHasSharedChildren());
		Assert.assertTrue(dir1.isSharedOrHasSharedChildren());
		Assert.assertTrue(dir2.isSharedOrHasSharedChildren());

		Assert.assertFalse(child1.isSharedOrHasSharedChildren());
		Assert.assertFalse(child2.isSharedOrHasSharedChildren());
		Assert.assertFalse(child3.isSharedOrHasSharedChildren());

		// set 2d to be not shared
		dir2.unshare();

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
		Assert.assertEquals(dir3, dir1.getChildByName("3d"));
		Assert.assertEquals(dir4, dir3.getChildByName("4d"));
		Assert.assertEquals(null, root.getChildByName("2f"));
		Assert.assertEquals(null, root.getChildByName(null));
		Assert.assertEquals(null, root.getChildByName(""));
	}

	@Test
	public void testPermissions() {
		Assert.assertTrue(root.getCalculatedUserList().contains(userId));
		Assert.assertEquals(1, root.getCalculatedUserList().size());

		// add permission to sub-folder
		dir1.share(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE));
		dir1.addUserPermissions(new UserPermission(userId, PermissionType.WRITE));
		dir1.addUserPermissions(new UserPermission("UserB", PermissionType.READ));

		// check the sub-folder and the sub-files permission
		Assert.assertEquals(2, dir1.getCalculatedUserList().size());
		Assert.assertEquals(2, child3.getCalculatedUserList().size());
		Assert.assertEquals(2, dir2.getCalculatedUserList().size());
		Assert.assertEquals(2, dir3.getCalculatedUserList().size());
		Assert.assertEquals(2, dir4.getCalculatedUserList().size());
		Assert.assertTrue(dir1.canWrite("UserA"));
		Assert.assertFalse(dir1.canWrite("UserB"));
		Assert.assertTrue(dir2.canWrite("UserA"));
		Assert.assertFalse(dir2.canWrite("UserB"));
		Assert.assertTrue(dir3.canWrite("UserA"));
		Assert.assertFalse(dir3.canWrite("UserB"));
		Assert.assertTrue(dir4.canWrite("UserA"));
		Assert.assertFalse(dir4.canWrite("UserB"));

		// validate that the root still has only one user
		Assert.assertTrue(root.getCalculatedUserList().contains(userId));
		Assert.assertEquals(1, root.getCalculatedUserList().size());

		// add a third permission to the dir1
		dir1.addUserPermissions(new UserPermission("UserC", PermissionType.WRITE));

		// check again
		Assert.assertEquals(3, dir1.getCalculatedUserList().size());
		Assert.assertEquals(3, child3.getCalculatedUserList().size());
		Assert.assertEquals(3, dir2.getCalculatedUserList().size());
		Assert.assertEquals(3, dir3.getCalculatedUserList().size());
		Assert.assertEquals(3, dir4.getCalculatedUserList().size());
		Assert.assertTrue(dir1.canWrite("UserC"));
		Assert.assertTrue(dir2.canWrite("UserC"));
		Assert.assertTrue(dir3.canWrite("UserC"));
		Assert.assertTrue(dir4.canWrite("UserC"));
	}
}
