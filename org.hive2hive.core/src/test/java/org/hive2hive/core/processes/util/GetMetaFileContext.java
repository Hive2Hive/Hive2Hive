package org.hive2hive.core.processes.util;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Helper class to get the meta file
 * 
 * @author Nico
 */
public class GetMetaFileContext implements IGetMetaFileContext {

	private final KeyPair keys;
	public MetaFile metaFile;

	public GetMetaFileContext(KeyPair keys) {
		this.keys = keys;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return keys;
	}
}