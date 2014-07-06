package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.context.interfaces.IFile2MetaContext;
import org.hive2hive.core.security.HybridEncryptedContent;

public class RecoverFileContext implements IFile2MetaContext {

	private final File file;
	private KeyPair metaFileEncryptionKeys;
	private MetaFile metaFile;

	public RecoverFileContext(File file) {
		this.file = file;
	}

	@Override
	public File consumeFile() {
		return file;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		// not used here
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// not used here
	}

	@Override
	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys) {
		this.metaFileEncryptionKeys = encryptionKeys;
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return metaFileEncryptionKeys;
	}
}
