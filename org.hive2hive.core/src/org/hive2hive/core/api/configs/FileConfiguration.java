package org.hive2hive.core.api.configs;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;

public class FileConfiguration implements IFileConfiguration {

	private final long maxFileSize;
	private final long maxNumOfVersions;
	private final long maxSizeOfAllVersions;
	private final long chunkSize;

	private FileConfiguration(long maxFileSize, long maxNumOfVersions, long maxSizeAllVersions, long chunkSize) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeOfAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
	}

	public static IFileConfiguration createDefault() {
		return new FileConfiguration(H2HConstants.DEFAULT_MAX_FILE_SIZE,
				H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS, H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS,
				H2HConstants.DEFAULT_CHUNK_SIZE);
	}

	public static IFileConfiguration createCustom(long maxFileSize, long maxNumOfVersions,
			long maxSizeAllVersions, long chunkSize) {
		return new FileConfiguration(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize);
	}

	@Override
	public long getMaxFileSize() {
		return maxFileSize;
	}

	@Override
	public long getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	@Override
	public long getMaxSizeAllVersions() {
		return maxSizeOfAllVersions;
	}

	@Override
	public long getChunkSize() {
		return chunkSize;
	}
}
