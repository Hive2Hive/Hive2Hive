package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.api.ProcessManager;

public interface INewH2HNode {

	void connect();
	
	void disconnect();
	
	INetworkConfiguration getNetworkConfiguration();
	
	ProcessManager getProcessManager();

	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	IFileConfiguration getFileConfiguration();
	
	// TODO getStatus();
	
}
