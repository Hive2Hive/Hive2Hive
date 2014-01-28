package org.hive2hive.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

public class DeleteFileProcessContext implements IProvideMetaDocument, IConsumeMetaDocument, IProvideProtectionKeys, IConsumeProtectionKeys {

	private final boolean isDirectory;
	private HybridEncryptedContent encryptedMetaDocument;

	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	
	public DeleteFileProcessContext(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	
	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public boolean isDirectory() {
		return isDirectory;
	}
	
	public void setEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		this.encryptedMetaDocument = encryptedMetaDocument;
	}

	public HybridEncryptedContent getEncryptedMetaDocument() {
		return encryptedMetaDocument;
	}
}
