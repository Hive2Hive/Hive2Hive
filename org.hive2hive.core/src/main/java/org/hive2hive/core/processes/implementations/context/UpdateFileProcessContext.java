package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFileSmall;

public class UpdateFileProcessContext extends AddFileProcessContext {

	// the chunk keys to delete (if the configuration does not allow as many or as big chunks as existent)
	private List<MetaChunk> chunksToDelete;

	public UpdateFileProcessContext(File file) {
		super(file);
	}

	public List<MetaChunk> getChunksToDelete() {
		return chunksToDelete;
	}

	public void setChunksToDelete(List<MetaChunk> chunksToDelete) {
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public KeyPair consumeChunkKeys() {
		return ((MetaFileSmall) consumeMetaFile()).getChunkKey();
	}

}
