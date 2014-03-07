package org.hive2hive.core.api.configs;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;

/**
 * A file configuration such that the peers know how to handle file uploads, chunking and cleanups. This
 * configuration must be constant for all peers in the DHT.
 * 
 * @author Nico
 * 
 */
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

	/**
	 * Creates a default file configuration
	 * 
	 * @return
	 */
	public static IFileConfiguration createDefault() {
		return new FileConfiguration(H2HConstants.DEFAULT_MAX_FILE_SIZE,
				H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS, H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS,
				H2HConstants.DEFAULT_CHUNK_SIZE);
	}

	/**
	 * Create a file configuration with the given parameters
	 * 
	 * @param maxFileSize the maximum file size (in bytes)
	 * @param maxNumOfVersions the allowed number of versions
	 * @param maxSizeAllVersions the maximum file size when summing up all versions (in bytes)
	 * @param chunkSize the size of a chunk (in bytes)
	 */
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
