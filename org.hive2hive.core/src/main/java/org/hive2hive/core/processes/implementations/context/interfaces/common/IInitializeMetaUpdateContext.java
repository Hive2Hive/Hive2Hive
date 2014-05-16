package org.hive2hive.core.processes.implementations.context.interfaces.common;

import java.security.KeyPair;

import org.hive2hive.core.model.Index;

public interface IInitializeMetaUpdateContext {

	public Index consumeIndex();

	public KeyPair consumeOldProtectionKeys();

	public KeyPair consumeNewProtectionKeys();

}
