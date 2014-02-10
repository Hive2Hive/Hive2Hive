package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.processes.ProcessManager;

public interface INewH2HNode {

	INetworkConfiguration getNetworkConfiguration();
	
	ProcessManager getProcessManager();

	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	IFileConfiguration getFileConfiguration();
	
}
