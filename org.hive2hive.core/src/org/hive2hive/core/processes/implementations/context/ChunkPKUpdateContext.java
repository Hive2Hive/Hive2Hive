package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.security.H2HEncryptionUtil;

public class ChunkPKUpdateContext extends BasePKUpdateContext {

	private final PublicKey chunkKey;

	public ChunkPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, PublicKey chunkKey) {
		super(oldProtectionKeys, newProtectionKeys);
		this.chunkKey = chunkKey;
	}

	@Override
	public String getLocationKey() {
		return H2HEncryptionUtil.key2String(chunkKey);
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
