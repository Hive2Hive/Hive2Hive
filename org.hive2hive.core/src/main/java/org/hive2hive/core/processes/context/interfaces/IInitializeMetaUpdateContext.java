package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.Index;

public interface IInitializeMetaUpdateContext {

	Index consumeIndex();

	KeyPair consumeOldProtectionKeys();

	KeyPair consumeNewProtectionKeys();

	boolean isSharedBefore();

}
