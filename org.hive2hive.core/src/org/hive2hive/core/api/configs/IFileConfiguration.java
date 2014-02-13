package org.hive2hive.core.api.configs;

public interface IFileConfiguration {

	long getMaxFileSize();

	long getMaxNumOfVersions();

	// TODO this actually can be calculated from the other two values
	long getMaxSizeAllVersions();

	long getChunkSize();
	
}
