package org.hive2hive.core.utils;

import java.math.BigInteger;

import org.hive2hive.core.api.interfaces.IFileConfiguration;

/**
 * File configuration for fast test execution
 * 
 * @author Nico
 *
 */
public class TestFileConfiguration implements IFileConfiguration {

	// for fast access
	public static final int CHUNK_SIZE = 64;

	@Override
	public BigInteger getMaxFileSize() {
		return BigInteger.valueOf(getChunkSize() * 5);
	}

	@Override
	public int getMaxNumOfVersions() {
		return 5;
	}

	@Override
	public BigInteger getMaxSizeAllVersions() {
		return BigInteger.valueOf(getMaxNumOfVersions() * getChunkSize());
	}

	@Override
	public int getChunkSize() {
		return CHUNK_SIZE;
	}

}
