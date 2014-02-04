package org.hive2hive.core.test.processes.util;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Helper class to get the meta document
 * 
 * @author Nico
 * 
 */
public class GetMetaDocumentContext implements IConsumeKeyPair, IProvideMetaDocument {

	private final KeyPair keys;
	public MetaDocument metaDocument;

	public GetMetaDocumentContext(KeyPair keys) {
		this.keys = keys;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		// ignore
	}

	@Override
	public KeyPair consumeKeyPair() {
		return keys;
	}
}