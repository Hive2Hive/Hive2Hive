package org.hive2hive.core.test.integration;

import org.hive2hive.core.IH2HFileConfiguration;

public class TestH2HFileConfiguration implements IH2HFileConfiguration {

	@Override
	public int getMaxFileSize() {
		return 1024 * 1024;
	}

	@Override
	public int getMaxNumOfVersions() {
		return 10;
	}

	@Override
	public int getMaxSizeAllVersions() {
		return getMaxFileSize() * getMaxNumOfVersions();
	}

	@Override
	public int getChunkSize() {
		return 1024;
	}

}
