package org.hive2hive.core.api.interfaces;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;

public interface IUserManager extends INetworkNode {

	IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException;
	
	// TODO the file root path should not be part of this interface, but be placed in IFileManagement
	IProcessComponent login(UserCredentials credentials, IFileConfiguration fileConfig, Path rootPath) throws NoPeerConnectionException;
	
	// TODO why not logout with credentials as well?
	IProcessComponent logout() throws NoPeerConnectionException, NoSessionException;

}
