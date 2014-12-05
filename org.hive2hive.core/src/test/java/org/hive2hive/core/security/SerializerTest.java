package org.hive2hive.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.hive2hive.core.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SerializerTest extends H2HJUnitTest {

	private final IH2HSerialize serializer;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SerializerTest.class;
		beforeClass();
	}

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters(name = "{0}")
	public static Collection data() {
		return Arrays.asList(new Object[][] { { new JavaSerializer() }, { new FSTSerializer() } });
	}

	public SerializerTest(IH2HSerialize serializer) {
		this.serializer = serializer;
	}

	@Test
	public void serializationTest() throws IOException, ClassNotFoundException {
		String data = randomString(1000);
		logger.debug("Testing data serialization.");
		logger.debug("Test String: {}.", data);

		byte[] serializedData = serializer.serialize(data);
		assertNotNull(serializedData);
		assertNotEquals(0, serializedData.length);
		printBytes("Serialized Data:", serializedData);

		String deserializedData = (String) serializer.deserialize(serializedData);
		assertNotNull(deserializedData);
		assertEquals(data, deserializedData);
	}

	@Test
	public void nullSerializationTest() throws IOException, ClassNotFoundException {
		logger.debug("Testing null serialization.");

		// serializing results in non-null value
		byte[] serialized = serializer.serialize(null);
		assertNotNull(serialized);

		// when deserializing, it's again null
		assertNull(serializer.deserialize(serialized));

		// however, deseriazlizing null is not allowed
		assertNull(serializer.deserialize(null));
	}

	@AfterClass
	public static void endTest() throws Exception {
		afterClass();
	}
}
