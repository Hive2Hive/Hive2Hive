package org.hive2hive.processes.test.util;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;

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