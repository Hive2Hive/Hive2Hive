package org.hive2hive.core.test.model;

import org.hive2hive.core.model.UserCredentials;
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
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();

		// test if same result twice
		Assert.assertEquals(credentials.getProfileLocationKey(), credentials.getProfileLocationKey());
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
