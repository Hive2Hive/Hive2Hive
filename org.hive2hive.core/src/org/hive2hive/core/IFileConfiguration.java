package org.hive2hive.core;

public interface IFileConfiguration {

	long getMaxFileSize();

	long getMaxNumOfVersions();

	long getMaxSizeAllVersions();

	long getChunkSize();
}
