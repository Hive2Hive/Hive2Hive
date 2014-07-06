package org.hive2hive.core.processes.context.interfaces;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;

public interface ICheckWriteAccessContext {

	public File consumeFile();

	public Path consumeRoot();

	public void provideProtectionKeys(KeyPair protectionKeys);

}
