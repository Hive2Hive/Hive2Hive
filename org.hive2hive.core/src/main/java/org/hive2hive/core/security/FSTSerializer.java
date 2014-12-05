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
 * Fast serialization using the
 * <a href="https://github.com/RuedigerMoeller/fast-serialization">FST library</a>.
 * 
 * @author Nico, Chris
 * 
 */
public final class FSTSerializer implements IH2HSerialize {

	private static final Logger logger = LoggerFactory.getLogger(FSTSerializer.class);

	// thread safe usage of FST
	private final FSTConfiguration fst;

	public FSTSerializer() {
		fst = FSTConfiguration.createDefaultConfiguration();
	}

	@Override
	public byte[] serialize(Serializable object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FSTObjectOutput fstOut = fst.getObjectOutput(baos);
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

	@Override
	public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			// nothing to deserialize
			return null;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		FSTObjectInput fstIn = fst.getObjectInput(bais);
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
