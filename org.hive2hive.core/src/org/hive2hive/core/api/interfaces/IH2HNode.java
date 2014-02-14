package org.hive2hive.core.api.interfaces;


public interface IH2HNode {

	boolean connect();
	
	boolean disconnect();
	
	IUserManager getUserManager();
	
	IFileManager getFileManager();
	
	INetworkConfiguration getNetworkConfiguration();
	
	IFileConfiguration getFileConfiguration();
}
