package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.HybridEncryptedContent;

public class RecoverFileContext implements IProvideMetaDocument, IConsumeMetaDocument,
		IProvideProtectionKeys, IConsumeProtectionKeys {

	private final File file;
	private KeyPair protectionKeys;
	private MetaDocument metaDocument;

	public RecoverFileContext(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}
}
