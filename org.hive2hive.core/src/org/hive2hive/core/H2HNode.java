package org.hive2hive.core;

public class H2HNode {

	private final int maxFileSize;
	private final TimeToLiveStore ttlStore;
	private final int maxNumOfVersions;
	private final int maxSizeAllVersions;
	private final int chunkSize;

	public H2HNode(int maxFileSize, int maxNumOfVersions, int maxSizeAllVersions, int chunkSize,
			TimeToLiveStore ttlStore) {
		this.maxFileSize = maxFileSize;
		this.maxNumOfVersions = maxNumOfVersions;
		this.maxSizeAllVersions = maxSizeAllVersions;
		this.chunkSize = chunkSize;
		this.ttlStore = ttlStore;
	}

	public TimeToLiveStore getTimeToLiveStore() {
		return ttlStore;
	}

	public int getMaxFileSize() {
		return maxFileSize;
	}

	public int getMaxNumOfVersions() {
		return maxNumOfVersions;
	}

	public int getMaxSizeAllVersions() {
		return maxSizeAllVersions;
	}

	public int getChunkSize() {
		return chunkSize;
	}
}
