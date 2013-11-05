package org.hive2hive.core;

import java.net.InetAddress;

public class H2HNodeBuilder {

	// File size configuration
	private int maxFileSize = 25 * 1024 * 1024; // 25Mb
	private int maxNumOfVersions = 100;
	private int maxSizeAllVersions = maxFileSize * maxNumOfVersions;
	private int chunkSize = 1024 * 1024; // 1Mb
	private boolean autostartProcesses = true;
	private boolean isMaster = false;
	private InetAddress bootstrapAddress = null;

	public IH2HNode build() {
		return new H2HNode(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize, autostartProcesses,
				isMaster, bootstrapAddress);
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

	public H2HNodeBuilder setAutostartProcesses(boolean autostart) {
		this.autostartProcesses = autostart;
		return this;
	}

	public H2HNodeBuilder setMaster(boolean isMaster) {
		this.isMaster = isMaster;
		return this;
	}

	public H2HNodeBuilder setBootstrapAddress(InetAddress bootstrapAddress) {
		this.bootstrapAddress = bootstrapAddress;
		return this;
	}

}
