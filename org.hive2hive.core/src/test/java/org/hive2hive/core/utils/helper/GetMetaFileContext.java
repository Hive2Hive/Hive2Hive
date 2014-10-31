package org.hive2hive.core.utils.helper;

import java.security.KeyPair;

import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;

/**
 * Helper class to get the meta file
 * 
 * @author Nico
 */
public class GetMetaFileContext implements IGetMetaFileContext {

	private final KeyPair keys;
	public BaseMetaFile metaFile;

	public GetMetaFileContext(KeyPair keys) {
		this.keys = keys;
	}

	@Override
	public void provideMetaFile(BaseMetaFile metaFile) {
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