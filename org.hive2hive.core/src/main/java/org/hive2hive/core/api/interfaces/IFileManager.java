package org.hive2hive.core.api.interfaces;

import java.io.File;

import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Basic interface for all file management operations.
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
	 * @param file The file or folder to add.
	 * @return A file add process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createAddProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException;

	/**
	 * Delete a file / folder and all versions of that file from the network. This operation deletes also the
	 * file on disk. <strong>Note that this operation is irreversible.</strong> If the folder is not empty,
	 * all sub-files are deleted as well.
	 * 
	 * @param file The file or folder to delete.
	 * @return A file delete process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createDeleteProcess(File file) throws NoSessionException, NoPeerConnectionException,
			IllegalArgumentException;

	/**
	 * Update a file and create a new version. Folders cannot be updated.<br>
	 * 
	 * @param file The file to update.
	 * @return A file update process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createUpdateProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException;

	/**
	 * Download a file that exists in the network and store it on the disk. If the file is a folder, a folder
	 * on disk is created, but containing files are not downloaded automatically.<br>
	 * <strong>Note:</strong>If the file on disk already exists, it will be overwritten.
	 * 
	 * @param file The file to be downloaded.
	 * @return A file download process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createDownloadProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException;

	/**
	 * Move a file / folder from a given source to a given destination. This operation can also be used to
	 * rename a file, or moving and renaming it together. In case of moving a folder, sub-files are moved too.
	 * Note that this call does not perform any change on the file system.
	 * 
	 * @param source The full file path of the file to be moved.
	 * @param destination The full file path of the file destination.
	 * @return A file move process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createMoveProcess(File source, File destination) throws NoPeerConnectionException,
			NoSessionException, IllegalArgumentException;

	/**
	 * Recover a file version from the network and restore it under a new file (name is indicated with special
	 * suffix). The file is saved at the preferred location, but not automatically added to the network. If
	 * you want to synchronize that file, too, you need to call {@link #createAddProcess(File)} after this
	 * process
	 * succeeded.
	 * 
	 * @param file The file to be recovered.
	 * @param versionSelector The file version selector to be used.
	 * @return A file recover process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createRecoverProcess(File file, IVersionSelector versionSelector)
			throws NoPeerConnectionException, NoSessionException, IllegalArgumentException;

	/**
	 * Share a folder with a friend giving him read-only or write permission. The friend get's notified about
	 * the share and can see all contents of that folder. Note that this operation is irreversible, unsharing
	 * is currently only supported by deleting the whole folder.
	 * 
	 * @param folder The folder to be shared.
	 * @param userId The user ID of the new sharer.
	 * @param permission The permission type for the new sharer.
	 * @return A file share process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 * @throws IllegalArgumentException If the provided parameters are incorrect.
	 */
	IProcessComponent<Void> createShareProcess(File folder, String userId, PermissionType permission)
			throws NoPeerConnectionException, NoSessionException, IllegalArgumentException;

	/**
	 * Get a tree of all files in the DHT of the currently logged in user. This must not necessary match
	 * with the file tree on disk because Hive2Hive only performs file operations at manual calls.
	 * 
	 * @return A file list process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If no user has logged in.
	 */
	IProcessComponent<FileNode> createFileListProcess() throws NoPeerConnectionException, NoSessionException;

	/**
	 * Subscribe all file event handlers of the given listener instance.
	 * <strong>Note:</strong> The listener needs to annotate the handlers with the @Handler annotation.
	 * 
	 * @param listener implementing the handler methods
	 */
	void subscribeFileEvents(IFileEventListener listener);

}
