package org.hive2hive.core.integration;

import java.math.BigInteger;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;

public class TestFileConfiguration implements IFileConfiguration {

	@Override
	public BigInteger getMaxFileSize() {
		return H2HConstants.MEGABYTES;
	}

	@Override
	public int getMaxNumOfVersions() {
		return 10;
	}

	@Override
	public BigInteger getMaxSizeAllVersions() {
		return getMaxFileSize().multiply(BigInteger.valueOf(getMaxNumOfVersions()));
	}

	@Override
	public int getChunkSize() {
		return 1024;
	}

}
