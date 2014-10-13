package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;

public interface IGetMetaFileContext {

	public KeyPair consumeMetaFileEncryptionKeys();

	public void provideMetaFile(BaseMetaFile metaFile);

	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaFile);

}
