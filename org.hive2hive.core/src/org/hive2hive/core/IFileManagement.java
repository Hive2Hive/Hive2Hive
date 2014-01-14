package org.hive2hive.core;

import java.io.File;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.digest.IGetFileListProcess;

/**
 * Interface on all file operations that Hive2Hive currently supports.
 * 
 * @author Nico, Seppi
 * 
 */
public interface IFileManagement {

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

	/**
	 * Returns the file configuration. If you want to change this, use the {@link H2HNodeBuilder} when
	 * creating the {@link H2HNode}.
	 * 
	 * @return the file configuration of this node
	 */
	IFileConfiguration getFileConfiguration();

	/**
	 * Returns a list of the files currently stored in the network.
	 * 
	 * @return the observable process which additionally allows to get the result (digest) after finish
	 */
	IGetFileListProcess getFileList() throws NoSessionException;

	/**
	 * Shares a folder and all it's children with another user.
	 * 
	 * @param folder the folder to share
	 * @param userId the id of the user which will get access to the folder
	 * @return an observable share process
	 */
	IProcess share(File folder, String userId) throws IllegalArgumentException, NoSessionException,
			IllegalFileLocation;
}
