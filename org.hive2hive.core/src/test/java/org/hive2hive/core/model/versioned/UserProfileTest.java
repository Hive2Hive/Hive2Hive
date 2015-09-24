package org.hive2hive.core.model.versioned;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserProfileTest extends H2HJUnitTest {

	private UserProfile userProfile;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createUserProfile() {
		userProfile = new UserProfile(randomString(), generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS),
				generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION));
	}

	@Test
	public void testGetFileById() {
		FolderIndex root = userProfile.getRoot();

		KeyPair child1Key = generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		FolderIndex child1Folder = new FolderIndex(root, child1Key, randomString());

		KeyPair child2Key = generateRSAKeyPair(RSA_KEYLENGTH.BIT_1024);
		new FileIndex(root, child2Key, randomString(), "bla".getBytes());

		KeyPair child3Key = generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		new FileIndex(child1Folder, child3Key, randomString(), "blubb".getBytes());

		Assert.assertNotNull(userProfile.getFileById(child1Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child2Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child3Key.getPublic()));
	}

	@Test
	public void getFileByPath() throws IOException {
		FolderIndex root = userProfile.getRoot();

		// tree in the UP
		FolderIndex folderIndex1 = new FolderIndex(root, generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), randomString());
		FileIndex fileIndex1 = new FileIndex(folderIndex1, generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), randomString(),
				"bla".getBytes());
		FileIndex fileIndex2 = new FileIndex(folderIndex1, generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), randomString(),
				"blubb".getBytes());
		FolderIndex folderIndex2 = new FolderIndex(folderIndex1, generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), randomString());
		FileIndex fileIndex3 = new FileIndex(folderIndex2, generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), randomString(),
				"bla".getBytes());

		// tree on the FS
		File rootFile = tempFolder.newFolder();
		File folder1 = new File(rootFile, folderIndex1.getName());
		File file1 = new File(folder1, fileIndex1.getName());
		File file2 = new File(folder1, fileIndex2.getName());
		File folder2 = new File(folder1, folderIndex2.getName());
		File file3 = new File(folder2, fileIndex3.getName());

		Assert.assertEquals(root, userProfile.getFileByPath(rootFile, rootFile));
		Assert.assertEquals(folderIndex1, userProfile.getFileByPath(folder1, rootFile));
		Assert.assertEquals(fileIndex1, userProfile.getFileByPath(file1, rootFile));
		Assert.assertEquals(fileIndex2, userProfile.getFileByPath(file2, rootFile));
		Assert.assertEquals(folderIndex2, userProfile.getFileByPath(folder2, rootFile));
		Assert.assertEquals(fileIndex3, userProfile.getFileByPath(file3, rootFile));
	}
}
