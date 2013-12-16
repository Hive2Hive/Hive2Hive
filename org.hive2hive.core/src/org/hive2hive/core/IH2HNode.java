package org.hive2hive.core;

import java.io.File;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
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
	 */
	IProcess login(UserCredentials credentials, File rootPath);

	/**
	 * Initiates and returns a logout process.
	 * 
	 * @return the observable logout process.
	 */
	IProcess logout() throws NoSessionException;

	/**
	 * Add a file or a folder to the network. Note that the file must be within the root directory given in
	 * the node configuration. If a full tree needs to be uploaded, the parameter must be the root.
	 * 
	 * @param file the file to be added
	 * @return an observable add file process
	 */
	IProcess add(File file) throws IllegalFileLocation, NoSessionException;

	/**
	 * Update a file or a folder in the network.
	 * 
	 * @param file the file to be updated
	 * @return an observable update process
	 */
	IProcess update(File file) throws NoSessionException, IllegalArgumentException;

	/**
	 * Moves a file from source to destination
	 * 
	 * @param source the file to move
	 * @param destination the destination of the file
	 * @return an observable move process
	 */
	IProcess move(File source, File destination) throws NoSessionException, IllegalArgumentException;

	/**
	 * Delete the file or the folder in the network. Note that when a whole file tree should be deleted, the
	 * parameter must be the root.
	 * 
	 * @param the file to delete
	 * @return an observable deletion process
	 */
	IProcess delete(File file) throws IllegalArgumentException, NoSessionException;

	// TODO this must not be part of the H2H interface
	void disconnect();
}
