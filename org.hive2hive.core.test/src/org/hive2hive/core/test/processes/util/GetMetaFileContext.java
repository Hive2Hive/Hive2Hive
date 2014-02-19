package org.hive2hive.core.test.processes.util;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Helper class to get the meta file
 * 
 * @author Nico
 * 
 */
public class GetMetaFileContext implements IConsumeKeyPair, IProvideMetaFile {

	private final KeyPair keys;
	public MetaFile metaDocument;

	public GetMetaFileContext(KeyPair keys) {
		this.keys = keys;
	}

	@Override
	public void provideMetaFile(MetaFile metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore
	}

	@Override
	public KeyPair consumeKeyPair() {
		return keys;
	}
}