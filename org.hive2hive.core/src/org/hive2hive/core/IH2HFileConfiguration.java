package org.hive2hive.core;

public interface IH2HFileConfiguration {

	int getMaxFileSize();

	int getMaxNumOfVersions();

	int getMaxSizeAllVersions();

	int getChunkSize();
}
