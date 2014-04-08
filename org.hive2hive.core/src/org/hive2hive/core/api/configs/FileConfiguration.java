package org.hive2hive.core.api.configs;

import java.math.BigInteger;

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

	private final BigInteger maxFileSize;
	private final int maxNumOfVersions;
	private final BigInteger maxSizeOfAllVersions;
	private final int chunkSize;

	private FileConfiguration(BigInteger maxFileSize, int maxNumOfVersions, BigInteger maxSizeAllVersions,
			int chunkSize) {
		assert maxFileSize.signum() == 1;
		assert maxNumOfVersions > 0;
		assert maxSizeAllVersions.signum() == 1;
		assert chunkSize > 0;

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
	public static IFileConfiguration createCustom(BigInteger maxFileSize, int maxNumOfVersions,
			BigInteger maxSizeAllVersions, int chunkSize) {
		return new FileConfiguration(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize);
	}

	@Override
	public BigInteger getMaxFileSize() {
		return maxFileSize;
	}

	@Override
	public int getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	@Override
	public BigInteger getMaxSizeAllVersions() {
		return maxSizeOfAllVersions;
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}
}
