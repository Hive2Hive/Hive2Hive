package org.hive2hive.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.hive2hive.core.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SerializationUtilTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SerializationUtilTest.class;
		beforeClass();
	}

	@Test
	public void serializationTest() throws IOException, ClassNotFoundException {
		String data = generateRandomString(1000);
		logger.debug("Testing data serialization.");
		logger.debug("Test String: {}.", data);

		byte[] serializedData = SerializationUtil.serialize(data);
		assertNotNull(serializedData);
		assertNotEquals(0, serializedData.length);
		printBytes("Serialized Data:", serializedData);

		String deserializedData = (String) SerializationUtil.deserialize(serializedData);
		assertNotNull(deserializedData);
		assertEquals(data, deserializedData);
	}

	@Test
	public void nullSerializationTest() throws IOException, ClassNotFoundException {
		logger.debug("Testing null serialization.");

		// serializing results in non-null value
		byte[] serialized = SerializationUtil.serialize(null);
		assertNotNull(serialized);

		// when deserializing, it's again null
		assertNull(SerializationUtil.deserialize(serialized));

		// however, deseriazlizing null is not allowed
		assertNull(SerializationUtil.deserialize(null));
	}

	@AfterClass
	public static void endTest() throws Exception {
		afterClass();
	}
}
