package org.hive2hive.core.tomp2p;

import java.util.Arrays;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Number160Test extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = Number160Test.class;
		beforeClass();
	}

	@Test
	public void testHashes() {
		// test two random literals
		String unrelated1 = NetworkTestUtil.randomString();
		Number160 num1 = Number160.createHash(unrelated1);
		String unrelated2 = NetworkTestUtil.randomString();
		Number160 num2 = Number160.createHash(unrelated2);

		Assert.assertFalse(Arrays.equals(num1.toByteArray(), num2.toByteArray()));

		// test two close literals
		Number160 ua = Number160.createHash("User A");
		Number160 ub = Number160.createHash("User B");
		Assert.assertFalse(Arrays.equals(ua.toByteArray(), ub.toByteArray()));

	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
