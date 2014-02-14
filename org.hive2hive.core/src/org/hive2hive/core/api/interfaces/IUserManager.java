package org.hive2hive.core.api.interfaces;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;

/**
 * Basic interface for all user operations.
 * 
 * @author Christian
 * 
 */
public interface IUserManager extends IManager {

	IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException;

	// TODO the file root path should not be part of this interface, but have a place in IFileManagement
	IProcessComponent login(UserCredentials credentials, Path rootPath) throws NoPeerConnectionException;

	// TODO why not logout with credentials as well?
	IProcessComponent logout() throws NoPeerConnectionException, NoSessionException;

	IFileConfiguration getFileConfiguration();

}
