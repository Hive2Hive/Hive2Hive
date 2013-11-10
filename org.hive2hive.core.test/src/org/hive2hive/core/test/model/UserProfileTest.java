package org.hive2hive.core.test.model;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
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
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		// test if same result twice
		Assert.assertEquals(UserProfile.getLocationKey(credentials), UserProfile.getLocationKey(credentials));
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
