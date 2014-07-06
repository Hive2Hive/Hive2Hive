package org.hive2hive.core.processes.context.interfaces;

import java.io.File;
import java.security.KeyPair;

public interface IGetFileKeysContext {
	
	public File consumeFile();

	public void provideProtectionKeys(KeyPair protectionKeys);

	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys);

}
