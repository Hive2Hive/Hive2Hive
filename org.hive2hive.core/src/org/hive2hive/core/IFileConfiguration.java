package org.hive2hive.core;

@Deprecated
public interface IFileConfiguration {

	long getMaxFileSize();

	long getMaxNumOfVersions();

	long getMaxSizeAllVersions();

	long getChunkSize();
}
