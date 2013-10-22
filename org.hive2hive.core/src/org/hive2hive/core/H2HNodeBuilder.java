package org.hive2hive.core;

public class H2HNodeBuilder {

	// File size configuration
	private int maxFileSize = 25 * 1024 * 1024; // 25Mb
	private int maxNumOfVersions = 100;
	private int maxSizeAllVersions = maxFileSize * maxNumOfVersions;
	private int chunkSize = 1024 * 1024; // 1Mb

	// time to live configuration
	private TimeToLiveStore ttlStore = new TimeToLiveStoreBuilder().build();

	public H2HNode build() {
		return new H2HNode(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize, ttlStore);
	}

	public H2HNodeBuilder setTimeToLiveStoreBuilder(TimeToLiveStore ttlStore) {
		this.ttlStore = ttlStore;
		return this;
	}

	/**
	 * Set the max size in bytes
	 * 
	 * @param fileSize in bytes
	 * @return the builder according to the builder pattern
	 */
	public H2HNodeBuilder setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
		return this;
	}

	public H2HNodeBuilder setMaxNumOfVersions(int maxNumOfVersions) {
		this.maxNumOfVersions = maxNumOfVersions;
		return this;
	}

	public H2HNodeBuilder setMaxSizeAllVersions(int maxSizeAllVersions) {
		this.maxSizeAllVersions = maxSizeAllVersions;
		return this;
	}

	public H2HNodeBuilder setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		return this;
	}

}
