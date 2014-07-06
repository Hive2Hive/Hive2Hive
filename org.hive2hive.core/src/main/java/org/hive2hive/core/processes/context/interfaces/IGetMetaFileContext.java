package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.security.HybridEncryptedContent;

public interface IGetMetaFileContext {

	public KeyPair consumeMetaFileEncryptionKeys();

	public void provideMetaFile(MetaFile metaFile);

	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaFile);

}
