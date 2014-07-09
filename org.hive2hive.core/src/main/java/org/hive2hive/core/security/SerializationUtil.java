package org.hive2hive.core.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

/**
 * Provides serialization and deserialization of objects
 * 
 * @author Nico, Chris
 * 
 */
public class SerializationUtil {

	private static final Logger logger = LoggerFactory.getLogger(SerializationUtil.class);

	// thread safe usage of FST
	private static final FSTConfiguration FST = FSTConfiguration.createDefaultConfiguration();

	private SerializationUtil() {
		// only static methods
	}

	/**
	 * Serializes an object to a byte array which can be sent over the network
	 */
	public static byte[] serialize(Serializable object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FSTObjectOutput fstOut = FST.getObjectOutput(baos);
		byte[] result = new byte[0];

		try {
			fstOut.writeObject(object);
			result = baos.toByteArray();
		} catch (IOException e) {
			logger.error("Exception while serializing object:", e);
			throw e;
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				logger.error("Exception while closing serialization process.", e);
			}
		}
		return result;
	}

	/**
	 * Deserializes an object from a byte array. The object type is not yet specific
	 */
	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			// nothing to deserialize
			return null;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		FSTObjectInput fstIn = FST.getObjectInput(bais);
		Object result = null;

		try {
			result = fstIn.readObject();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Exception while deserializing object.");
			throw e;
		} finally {
			try {
				if (bais != null) {
					bais.close();
				}
			} catch (IOException e) {
				logger.error("Exception while closing deserialization process.", e);
			}
		}

		return result;
	}
}
