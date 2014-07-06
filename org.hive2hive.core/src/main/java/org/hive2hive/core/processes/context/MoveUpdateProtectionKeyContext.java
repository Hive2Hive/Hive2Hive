package org.hive2hive.core.processes.context;

import java.security.KeyPair;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.context.interfaces.IInitializeMetaUpdateContext;

public class MoveUpdateProtectionKeyContext implements IInitializeMetaUpdateContext {

	private final Index index;
	private final KeyPair oldProtectionKeys;
	private final KeyPair newProtectionKeys;

	public MoveUpdateProtectionKeyContext(Index index, KeyPair oldProtectionKeys, KeyPair newProtectionKeys) {
		this.index = index;
		this.oldProtectionKeys = oldProtectionKeys;
		this.newProtectionKeys = newProtectionKeys;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}

	@Override
	public KeyPair consumeNewProtectionKeys() {
		return newProtectionKeys;
	}

	@Override
	public KeyPair consumeOldProtectionKeys() {
		return oldProtectionKeys;
	}
}
