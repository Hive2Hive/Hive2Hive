package org.hive2hive.core.test.model;

import java.security.KeyPair;

import org.hive2hive.core.model.IndexNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
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
		IndexNode root = userProfile.getRoot();

		KeyPair child1Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);
		IndexNode child1Folder = new IndexNode(root, child1Key, NetworkTestUtil.randomString());

		KeyPair child2Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_1024);
		new IndexNode(root, child2Key, NetworkTestUtil.randomString(), "bla".getBytes());

		KeyPair child3Key = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		new IndexNode(child1Folder, child3Key, NetworkTestUtil.randomString(), "blubb".getBytes());

		Assert.assertNotNull(userProfile.getFileById(child1Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child2Key.getPublic()));
		Assert.assertNotNull(userProfile.getFileById(child3Key.getPublic()));
	}
}
