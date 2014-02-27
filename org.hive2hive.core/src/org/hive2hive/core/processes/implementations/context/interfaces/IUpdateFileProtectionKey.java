package org.hive2hive.core.processes.implementations.context.interfaces;

import java.security.KeyPair;

public interface IUpdateFileProtectionKey extends IConsumeIndex {

	KeyPair consumeNewProtectionKeys();

	KeyPair consumeOldProtectionKeys();
}
