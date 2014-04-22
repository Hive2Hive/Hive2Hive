package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.HybridEncryptedContent;

public class RecoverFileContext implements IProvideMetaFile, IConsumeMetaFile, IProvideProtectionKeys,
		IConsumeProtectionKeys {

	private final File file;
	private KeyPair protectionKeys;
	private MetaFile metaFile;

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
	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}
}
