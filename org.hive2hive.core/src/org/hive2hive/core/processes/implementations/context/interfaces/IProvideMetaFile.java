package org.hive2hive.core.processes.implementations.context.interfaces;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.security.HybridEncryptedContent;

public interface IProvideMetaFile {

	void provideMetaFile(MetaFile metaFile);

	void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaFile);

}
