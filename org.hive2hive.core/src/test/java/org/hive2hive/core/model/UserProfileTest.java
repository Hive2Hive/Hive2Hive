package org.hive2hive.core.model;

import java.security.KeyPair;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.security.EncryptionUtil;
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
		testClass = MetaFileTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createUserProfile() {
		userProfile = new UserProfile(NetworkTestUtil.randomString());
	}

	@Test
	public void testGetFileById() {
		FolderIndex root = userProfile.getRoot();

		KeyPair child1Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		FolderIndex child1Folder = new FolderIndex(root, child1Key, NetworkTestUtil.randomString());

		KeyPair child2Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_1024);
		new FileIndex(root, child2Key, NetworkTestUtil.randomString(), "bla".getBytes());

		KeyPair child3Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		new FileIndex(child1Folder, child3Key, NetworkTestUtil.randomString(), "blubb".getBytes());

		Assert.assertNotNull(userProfile.getFileById(child1Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child2Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child3Key.getPublic()));
	}
}
