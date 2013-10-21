package org.hive2hive.core;

public class H2HNodeBuilder {

	private int fileSize = 10000;

	public H2HNodeBuilder() {

	}

	public H2HNodeBuilder setFileSize(int fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public H2HNode build() {
		return new H2HNode();
	}

}
