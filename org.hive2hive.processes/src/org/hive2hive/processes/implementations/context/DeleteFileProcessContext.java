package org.hive2hive.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

public class DeleteFileProcessContext implements IProvideMetaDocument, IProvideProtectionKeys {

	private MetaDocument metaDocument;
	private KeyPair protectionKeys;

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

}
