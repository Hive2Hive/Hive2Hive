package org.hive2hive.core.test.model;

import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the user credentials behavior
 * 
 * @author Nico
 * 
 */
public class UserCredentialsTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserCredentialsTest.class;
		beforeClass();
	}

	@Test
	public void generateLocations() {
		// random credentials
		String userName = NetworkTestUtil.randomString();
		String password = NetworkTestUtil.randomString();
		String pin = NetworkTestUtil.randomString();

		UserCredentials credentials1 = new UserCredentials(userName, password, pin);
		UserCredentials credentials2 = new UserCredentials(userName, password, pin);

		// test if same result twice
		Assert.assertEquals(credentials1.getProfileLocationKey(), credentials2.getProfileLocationKey());
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
