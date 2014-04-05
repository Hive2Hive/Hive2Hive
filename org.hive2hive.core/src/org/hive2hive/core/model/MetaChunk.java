package org.hive2hive.core.model;

import java.io.Serializable;

/**
 * Holds meta data of a chunk in the DHT
 * 
 * @author Seppi
 */
public class MetaChunk implements Serializable {

	private static final long serialVersionUID = -2463290285291070943L;

	private final String chunkId;
	private final byte[] chunkHash;

	public MetaChunk(String chunkId, byte[] chunkHash) {
		this.chunkId = chunkId;
		this.chunkHash = chunkHash;
	}

	public String getChunkId() {
		return chunkId;
	}

	public byte[] getChunkHash() {
		return chunkHash;
	}

}
