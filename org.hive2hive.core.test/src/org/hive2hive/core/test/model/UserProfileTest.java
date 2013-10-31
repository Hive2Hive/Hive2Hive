package org.hive2hive.core.test.model;

import java.security.KeyPair;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.UserPassword;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the user profile behavior
 * 
 * @author Nico
 * 
 */
public class UserProfileTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileTest.class;
		beforeClass();
	}

	@Test
	public void generateLocations() {
		// random credentials
		String userId = NetworkTestUtil.randomString();
		String password = NetworkTestUtil.randomString();
		String pin = NetworkTestUtil.randomString();

		// create profile and password
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		KeyPair domainKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		UserProfile profile = new UserProfile(userId, encryptionKeys, domainKeys);
		UserPassword userPassword = new UserPassword(password, pin);

		// test if same result twice
		Assert.assertEquals(profile.getLocationKey(userPassword), profile.getLocationKey(userPassword));
	}
}
