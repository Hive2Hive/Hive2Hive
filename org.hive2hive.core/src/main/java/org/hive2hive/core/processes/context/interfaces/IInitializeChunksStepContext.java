package org.hive2hive.core.processes.context.interfaces;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.MetaChunk;

public interface IInitializeChunksStepContext extends IPutSingleChunkContext {

	public File consumeFile();

	public boolean isLargeFile();

	public KeyPair consumeChunkKeys();

	public IFileConfiguration consumeFileConfiguration();

	public List<MetaChunk> getMetaChunks();

	public void provideChunkKeys(KeyPair chunkKeys);

}
