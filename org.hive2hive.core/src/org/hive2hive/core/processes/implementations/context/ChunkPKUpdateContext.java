package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;

public class ChunkPKUpdateContext extends BasePKUpdateContext {

	private final String chunkId;

	public ChunkPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, String chunkId) {
		super(oldProtectionKeys, newProtectionKeys);
		this.chunkId = chunkId;
	}

	@Override
	public String getLocationKey() {
		return chunkId;
	}

	@Override
	public String getContentKey() {
		return H2HConstants.FILE_CHUNK;
	}

	@Override
	public int getTTL() {
		return TimeToLiveStore.getInstance().getChunk();
	}

}
