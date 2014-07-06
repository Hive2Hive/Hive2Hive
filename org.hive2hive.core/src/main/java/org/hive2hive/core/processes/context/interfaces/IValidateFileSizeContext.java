package org.hive2hive.core.processes.context.interfaces;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileConfiguration;

public interface IValidateFileSizeContext {
	
	public File consumeFile();
	
	public boolean allowLargeFile();

	public IFileConfiguration consumeFileConfiguration();

	public void setLargeFile(boolean largeFile);

}
