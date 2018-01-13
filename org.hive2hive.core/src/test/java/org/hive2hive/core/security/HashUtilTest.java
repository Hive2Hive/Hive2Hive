package org.hive2hive.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;
import org.hive2hive.core.H2HJUnitTest;
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
	public void hashDataTest() {
		String data = randomString(1000);
		byte[] hash = HashUtil.hash(data.getBytes());
		assertNotNull(hash);

		// assert that hashing twice results in the same hash
		assertEquals(new String(hash), new String(HashUtil.hash(data.getBytes())));

		// assert that different data is hashed to different hashes
		String data2 = randomString(1000);
		assertNotEquals(data, data2);
		assertNotEquals(new String(hash), new String(HashUtil.hash(data2.getBytes())));
	}

	@Test
	public void hashExampleDataTest() {
		final String expected = "uU0nuZNNPgilLlLX2n2r+sSE7+N6U4DukIj3rOLvzek=";
		String data = "hello world";

		byte[] hash = HashUtil.hash(data.getBytes());
		String result = new String(Base64.encode(hash));

		assertEquals(expected, result);
	}

	@Test
	public void hashStreamTest() throws IOException {
		String data = randomString(5 * 1024);
		File file = new File(System.getProperty("java.io.tmpdir"), randomString());
		FileUtils.writeStringToFile(file, data);

		byte[] hash = HashUtil.hash(file);
		assertNotNull(hash);

		// assert that hashing twice results in the same hash
		assertEquals(new String(hash), new String(HashUtil.hash(file)));

		// assert that different data is hashed to different hashes
		String data2 = randomString(1000);
		assertNotEquals(data, data2);
		assertNotEquals(new String(hash), new String(HashUtil.hash(data2.getBytes())));
	}

	@Test
	public void hashStreamExampleDataTest() throws IOException {
		final String expected = "uU0nuZNNPgilLlLX2n2r+sSE7+N6U4DukIj3rOLvzek=";
		String data = "hello world";

		File file = new File(FileUtils.getTempDirectory(), randomString());
		FileUtils.writeStringToFile(file, data);

		byte[] hash = HashUtil.hash(file);
		String result = new String(Base64.encode(hash));

		assertEquals(expected, result);
	}

	@AfterClass
	public static void endTest() throws Exception {
		afterClass();
	}
}
