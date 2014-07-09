package org.hive2hive.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashUtilTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = HashUtilTest.class;
		beforeClass();
	}

	@Test
	public void md5DataTest() {
		String data = generateRandomString(1000);
		byte[] md5 = HashUtil.hash(data.getBytes());
		assertNotNull(md5);

		// assert that hashing twice results in the same md5 hash
		assertEquals(new String(md5), new String(HashUtil.hash(data.getBytes())));

		// assert that different data is hashed to different md5 hashes
		String data2 = generateRandomString(1000);
		assertNotEquals(data, data2);
		assertNotEquals(new String(md5), new String(HashUtil.hash(data2.getBytes())));
	}

	@Test
	public void md5StreamTest() throws IOException {
		String data = generateRandomString(5 * 1024);
		File file = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(file, data);

		byte[] md5 = HashUtil.hash(file);
		assertNotNull(md5);

		// assert that hashing twice results in the same md5 hash
		assertEquals(new String(md5), new String(HashUtil.hash(file)));

		// assert that different data is hashed to different md5 hashes
		String data2 = generateRandomString(1000);
		assertNotEquals(data, data2);
		assertNotEquals(new String(md5), new String(HashUtil.hash(data2.getBytes())));
	}

	@AfterClass
	public static void endTest() throws Exception {
		afterClass();
	}
}
