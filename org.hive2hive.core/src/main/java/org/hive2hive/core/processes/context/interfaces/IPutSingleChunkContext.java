package org.hive2hive.core.processes.context.interfaces;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.MetaChunk;

public interface IPutSingleChunkContext {

	File consumeFile();

	IFileConfiguration consumeFileConfiguration();

	KeyPair consumeChunkKeys();

	KeyPair consumeProtectionKeys();

	List<MetaChunk> getMetaChunks();

}
