package org.hive2hive.core;

import java.io.File;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.IProcess;

/**
 * Interface for all operations on a Hive2Hive peer. Note that all calls are returned immediately although the
 * process may still be running in the background. The returned process object can be used to control
 * the call.
 * 
 * @author Nico, Christian
 * 
 */
public interface IH2HNode {

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
	 * @return Returns an observable login process.
	 */
	IProcess login(UserCredentials credentials);

	/**
	 * Add a file or a folder to the network. Note that the file must be within the root directory given in
	 * the
	 * node configuration
	 */
	IProcess add(File file, UserCredentials credentials) throws IllegalFileLocation;

	void disconnect();
}
