package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.processes.ProcessManager;

public interface INewH2HNode {

	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	ProcessManager getProcessManager();
}
