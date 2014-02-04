package org.hive2hive.core;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;

/**
 * Interface on all user operations that Hive2Hive currently supports
 * 
 * @author Nico
 * 
 */
public interface IUserManagement {

	/**
	 * Initiates and returns a register process.
	 * 
	 * @param credentials The user's credentials with which it shall be registered.
	 * @return Returns an observable register process.
	 */
	IProcess register(UserCredentials credentials);

	/**
	 * Initiates and returns a login process.
	 * 
	 * @param credentials The user's credentials with which it shall be logged in.
	 * @param rootPath The user's root path to his files
	 * @return Returns an observable login process.
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent login(UserCredentials credentials, Path rootPath) throws NoPeerConnectionException;

	/**
	 * Initiates and returns a logout process.
	 * 
	 * @return the observable logout process.
	 */
	IProcess logout() throws NoSessionException;

}
