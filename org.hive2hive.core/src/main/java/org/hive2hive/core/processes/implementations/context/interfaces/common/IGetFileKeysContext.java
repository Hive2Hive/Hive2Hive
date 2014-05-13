package org.hive2hive.core.processes.implementations.context.interfaces.common;

import java.security.KeyPair;

public interface IGetFileKeysContext {

	void provideProtectionKeys(KeyPair protectionKeys);

	void provideMetaFileEncryptionKeys(KeyPair encryptionKeys);

}
