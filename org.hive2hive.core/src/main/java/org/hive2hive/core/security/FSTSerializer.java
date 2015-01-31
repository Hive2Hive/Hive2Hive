package org.hive2hive.core.security;

import java.io.IOException;
import java.io.Serializable;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		try {
			return fst.asByteArray(object);
		} catch (Throwable e) {
			logger.error("Exception while serializing object:", e);
			throw e;
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			// nothing to deserialize
			return null;
		}

		try {
			return fst.asObject(bytes);
		} catch (Throwable e) {
			logger.error("Exception while deserializing object.");
			throw e;
		}
	}
}
