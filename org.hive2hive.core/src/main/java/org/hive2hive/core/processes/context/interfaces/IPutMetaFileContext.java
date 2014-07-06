package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFile;

public interface IPutMetaFileContext {

	public MetaFile consumeMetaFile();

	public KeyPair consumeMetaFileProtectionKeys();

	public void provideMetaFileHash(byte[] hash);

}
