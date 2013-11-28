package org.hive2hive.core;

import java.net.InetAddress;

/**
 * A builder class for building specific types of {@link IH2HNode}s.
 * 
 * @author Christian
 * 
 */
public class H2HNodeBuilder {

	private int maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private int maxSizeOfAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;
	private boolean autostartProcesses = H2HConstants.DEFAULT_AUTOSTART_PROCESSES;
	private boolean isMasterPeer = H2HConstants.DEFAULT_IS_MASTER_PEER;
	private InetAddress bootstrapAddress = H2HConstants.DEFAULT_BOOTSTRAP_ADDRESS;
	private String rootPath = H2HConstants.DEFAULT_ROOT_PATH;

	public H2HNode build() {
		return new H2HNode(maxFileSize, maxNumOfVersions, maxSizeOfAllVersions, chunkSize,
				autostartProcesses, isMasterPeer, bootstrapAddress, rootPath);
	}

	public H2HNodeBuilder setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
		return this;
	}

	public H2HNodeBuilder setMaxNumOfVersions(int maxNumOfVersions) {
		this.maxNumOfVersions = maxNumOfVersions;
		return this;
	}

	public H2HNodeBuilder setMaxSizeAllVersions(int maxSizeOfAllVersions) {
		this.maxSizeOfAllVersions = maxSizeOfAllVersions;
		return this;
	}

	public H2HNodeBuilder setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		return this;
	}

	public H2HNodeBuilder setAutostartProcesses(boolean autostartProcesses) {
		this.autostartProcesses = autostartProcesses;
		return this;
	}

	public H2HNodeBuilder setIsMaster(boolean isMasterPeer) {
		this.isMasterPeer = isMasterPeer;
		return this;
	}

	public H2HNodeBuilder setBootstrapAddress(InetAddress bootstrapAddress) {
		this.bootstrapAddress = bootstrapAddress;
		return this;
	}

	public H2HNodeBuilder setRootPath(String rootPath) {
		this.rootPath = rootPath;
		return this;
	}
}