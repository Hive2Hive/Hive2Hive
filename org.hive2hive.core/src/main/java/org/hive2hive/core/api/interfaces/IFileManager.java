package org.hive2hive.core.api.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Basic interface for all file operations.
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface IFileManager {

	/**
	 * Add a file or a folder. Note that the file must already be in the predefined Hive2Hive folder. If the
	 * folder is not empty, containing files are <strong>not</strong> automatically added as well. The file
	 * must exist on the disk.
	 * 
	 * @param file the file / folder to add
	 * @return an observable process component
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 * @throws IllegalFileLocation the file is at a wrong location
	 */
	IProcessComponent<Void> add(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation;

	/**
	 * Delete a file / folder and all versions of that file from the network. This operation deletes also the
	 * file on disk. <strong>Note that this operation is irreversible.</strong> If the folder is not empty,
	 * all sub-files are deleted as well.
	 * 
	 * @param file the file / folder to delete. The file must still be on disk
	 * @return an observable process component
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> delete(File file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * Update a file and create a new version.<br>
	 * Note that the number of allowed versions can be configured in
	 * {@link IFileConfiguration#getMaxNumOfVersions()}.
	 * 
	 * @param file the file to update
	 * @return an observable process component
	 * @throws NoSessionException no user has logged in
	 * @throws IllegalArgumentException the file is at a wrong location or a folder. See details in the
	 *             exception description.
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> update(File file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * Download a file that exists in the network and store it on the disk. If the file is a folder, a folder
	 * on disk is created, but containing files are not downloaded automatically.<br>
	 * <strong>Note:</strong>If the file on disk already exists, it's content will be overwritten.
	 * 
	 * @param file the absolute file that should be downloaded. Use {@link #getFileList()} to get a list of
	 *            available files or register yourself at the event bus.
	 * @return an observable process component
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> download(File file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * Move a file / folder from a given source to a given destination. This operation can also be used to
	 * rename a file, or moving and renaming it together. In case of moving a folder, sub-files are moved too.
	 * Note that this call does not perform any change on the file system.
	 * 
	 * @param source the full path of the file to move
	 * @param destination the full path of the file destination
	 * @return an observable process component
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> move(File source, File destination) throws NoSessionException, NoPeerConnectionException;

	/**
	 * Recover a file version from the network and restore it under a new file (name is indicated with special
	 * suffix). The file is saved at the preferred location, but not automatically added to the network. If
	 * you want to synchronize that file, too, you need to call {@link #add(File)} after this process
	 * succeeded.
	 * 
	 * @param file the file to recover
	 * @param versionSelector selector to select a file version from the choice of all existing versions
	 *            (excluded the current one).
	 * @return an observable process component
	 * @throws FileNotFoundException if the file has not been found
	 * @throws IllegalArgumentException if the file is a folder (which cannot be versionized
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> recover(File file, IVersionSelector versionSelector) throws FileNotFoundException,
			NoSessionException, NoPeerConnectionException;

	/**
	 * Share a folder with a friend giving him read-only or write permission. The friend get's notified about
	 * the share and can see all contents of that folder. Note that this operation is irreversible, unsharing
	 * is currently only supported by deleting the whole folder.
	 * 
	 * @param folder the folder to share
	 * @param userId the unique user id of the new sharer
	 * @param permission the permission type (read-only or write access)
	 * @return
	 * @throws IllegalFileLocation if the folder is at an illegal location (not within the Hive2Hive
	 *             directory)
	 * @throws IllegalArgumentException if the folder cannot be shared for various reasons (check the detailed
	 *             error message in case it occurs). One possibility is that the folder is already in a shared
	 *             folder, which is currently not allowed in Hive2Hive.
	 * @throws NoSessionException no user has logged in
	 * @throws NoPeerConnectionException the peer has no connection to the network
	 */
	IProcessComponent<Void> share(File folder, String userId, PermissionType permission) throws IllegalFileLocation,
			NoSessionException, NoPeerConnectionException;

	/**
	 * Get a full list of all files in the DHT of the currently logged in user. This must not necessary match
	 * with the file tree on disk because Hive2Hive only performs file operations at manual calls.
	 * 
	 * @return an observable process component providing results of the request asynchronous.
	 * @throws NoSessionException no user has logged in
	 */
	IProcessComponent<List<FileTaste>> getFileList() throws NoSessionException;

	/**
	 * Subscribe all file event handlers of the given listener instance.
	 * <strong>Note:</strong> The listener needs to annotate the handlers with the @Handler annotation.
	 * 
	 * @param listener implementing the handler methods
	 */
	void subscribeFileEvents(IFileEventListener listener);

}
