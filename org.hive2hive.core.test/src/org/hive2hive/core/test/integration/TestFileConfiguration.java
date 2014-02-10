package org.hive2hive.core.test.integration;

import org.hive2hive.core.api.interfaces.IFileConfiguration;

public class TestFileConfiguration implements IFileConfiguration {

	@Override
	public long getMaxFileSize() {
		return 1024 * 1024;
	}

	@Override
	public long getMaxNumOfVersions() {
		return 10;
	}

	@Override
	public long getMaxSizeAllVersions() {
		return getMaxFileSize() * getMaxNumOfVersions();
	}

	@Override
	public long getChunkSize() {
		return 1024;
	}

}
