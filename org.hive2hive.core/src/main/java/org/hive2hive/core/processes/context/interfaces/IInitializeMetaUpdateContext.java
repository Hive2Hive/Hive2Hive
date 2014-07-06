package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.Index;

public interface IInitializeMetaUpdateContext {

	public Index consumeIndex();

	public KeyPair consumeOldProtectionKeys();

	public KeyPair consumeNewProtectionKeys();

}
