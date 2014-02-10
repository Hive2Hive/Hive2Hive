package org.hive2hive.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;

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
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent add(File file) throws IllegalFileLocation, NoSessionException,
			NoPeerConnectionException;

	/**
	 * Update a file or a folder in the network.
	 * 
	 * @param file the file to be updated
	 * @return an observable update process
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException;

	/**
	 * Moves a file from source to destination
	 * 
	 * @param source the file to move
	 * @param destination the destination of the file
	 * @return an observable move process
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent move(File source, File destination) throws NoSessionException,
			IllegalArgumentException, NoPeerConnectionException;

	/**
	 * Delete the file or the folder in the network. Note that when a whole file tree should be deleted, the
	 * parameter must be the root.
	 * 
	 * @param the file to delete
	 * @return an observable deletion process
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent delete(File file) throws IllegalArgumentException, NoSessionException,
			NoPeerConnectionException;

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
	IResultProcessComponent<List<Path>> getFileList() throws NoSessionException;

	/**
	 * Recover a previous file version
	 * 
	 * @param file the file to recover an old version from
	 * @param versionSelector selects a version to recover
	 * @return the observable process
	 * @throws NoPeerConnectionException
	 * @throws IllegalArgumentException
	 */
	IProcessComponent recover(File file, IVersionSelector versionSelector) throws NoSessionException,
			FileNotFoundException, IllegalArgumentException, NoPeerConnectionException;

	/**
	 * Shares a folder and all it's children with another user.
	 * 
	 * @param folder the folder to share
	 * @param userId the id of the user which will get access to the folder
	 * @param permission if the other user get read-only or write access
	 * @return an observable share process
	 * @throws NoPeerConnectionException
	 */
	IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalArgumentException, NoSessionException, IllegalFileLocation,
			NoPeerConnectionException;
}
