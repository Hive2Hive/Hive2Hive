package org.hive2hive.core;

import java.io.File;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.security.UserCredentials;

/**
 * Interface for all operations on a Hive2Hive peer. Note that all calls are returned immediately although the
 * process may still be running in the background. The returned process object can be used to control
 * the call.
 * 
 * @author Nico, Christian
 * 
 */
public interface IH2HNode {

	IProcess register(UserCredentials credentials);

	IProcess login(UserCredentials credentials);

	/**
	 * Add a file or a folder to the network. Note that the file must be within the root directory given in
	 * the
	 * node configuration
	 */
	IProcess add(File file) throws IllegalFileLocation;

	void disconnect();
}
