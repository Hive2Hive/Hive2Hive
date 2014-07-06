package org.hive2hive.core.processes.context;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.model.MetaChunk;

/**
 * Provides the required context to update a chunk
 * 
 * @author Nico, Seppi
 */
public class ChunkPKUpdateContext extends BasePKUpdateContext {

	private final MetaChunk metaChunk;

	public ChunkPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, MetaChunk metaChunk) {
		super(oldProtectionKeys, newProtectionKeys);
		this.metaChunk = metaChunk;
	}

	@Override
	public String getLocationKey() {
		return metaChunk.getChunkId();
	}

	@Override
	public String getContentKey() {
		return H2HConstants.FILE_CHUNK;
	}

	@Override
	public int getTTL() {
		return TimeToLiveStore.getInstance().getChunk();
	}
	
	@Override
	public byte[] getHash() {
		return metaChunk.getChunkHash();
	}

	@Override
	public Number160 getVersionKey() {
		return H2HConstants.TOMP2P_DEFAULT_KEY;
	}

}
