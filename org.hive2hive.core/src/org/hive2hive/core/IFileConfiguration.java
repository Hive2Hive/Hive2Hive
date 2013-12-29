package org.hive2hive.core;

public interface IFileConfiguration {

	int getMaxFileSize();

	int getMaxNumOfVersions();

	int getMaxSizeAllVersions();

	int getChunkSize();
}
