package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.api.ProcessManager;

public interface IH2HNode {

	void connect();
	
	void disconnect();
	
	INetworkConfiguration getNetworkConfiguration();
	
	ProcessManager getProcessManager();

	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	// TODO getStatus();
	
}
