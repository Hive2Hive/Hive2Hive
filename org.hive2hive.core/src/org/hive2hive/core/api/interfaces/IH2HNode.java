package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.api.ProcessManager;

public interface IH2HNode extends INetworkNode {
	
	ProcessManager getProcessManager();

	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	// TODO getStatus();
	
}
