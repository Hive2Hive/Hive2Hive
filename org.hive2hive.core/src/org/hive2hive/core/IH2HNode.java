package org.hive2hive.core;


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
	 * Returns the management interface for all user operations
	 * 
	 * @return the user management interface
	 */
	IUserManagement getUserManagement();

	/**
	 * Returns the management interface for all file operations
	 * 
	 * @return the file management interface
	 */
	IFileManagement getFileManagement();

	// TODO this must not be part of the H2H interface
	void disconnect();
}
