package org.hive2hive.core.api.interfaces;

public interface IFileConfiguration {

	long getMaxFileSize();

	long getMaxNumOfVersions();

	long getMaxSizeAllVersions();

	long getChunkSize();
	
}
