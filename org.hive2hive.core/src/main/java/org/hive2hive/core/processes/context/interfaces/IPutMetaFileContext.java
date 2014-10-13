package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.versioned.BaseMetaFile;

public interface IPutMetaFileContext {

	public BaseMetaFile consumeMetaFile();

	public KeyPair consumeMetaFileProtectionKeys();

	public void provideMetaFileHash(byte[] hash);

}
