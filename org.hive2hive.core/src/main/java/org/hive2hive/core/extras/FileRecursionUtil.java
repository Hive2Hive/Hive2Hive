package org.hive2hive.core.extras;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.processframework.composites.SyncProcess;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Build a recursive process to add / delete multiple files. When for example uploading a folder with
 * subfolders, etc., this can't happen concurrently since parent folders must already be uploaded.
 * 
 * @author Nico
 *
 */
@Extra
public class FileRecursionUtil {

	private FileRecursionUtil() {
		// only static methods
	}

	public enum FileProcessAction {
		NEW_FILE,
		MODIFY_FILE,
	}

	/**
	 * Creates an upload process. The order of the files does not depend.
	 * 
	 * @param files a list of files to upload
	 * @param action whether the files are for updating or as new files
	 * @param networkManager the network manager with a session
	 * @return the root process (containing multiple async components) that manages the upload correctly
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	public static IProcessComponent<Void> buildUploadProcess(List<File> files, FileProcessAction action,
			NetworkManager networkManager, IFileConfiguration fileConfiguration) throws NoSessionException,
			NoPeerConnectionException {
		// the root process
		SyncProcess rootProcess = new SyncProcess();

		// key idea: Find the children with the same parents and add them to a sequential process. They need
		// to be sequential because the parent meta file must be adapted. If they would run in parallel, they
		// would modify the parent meta folder simultaneously.
		Map<File, SyncProcess> sameParents = new HashMap<File, SyncProcess>();
		for (File file : files) {
			// create the process which uploads or updates the file
			IProcessComponent<Void> uploadProcess;
			if (action == FileProcessAction.NEW_FILE) {
				uploadProcess = ProcessFactory.instance().createAddFileProcess(file, networkManager, fileConfiguration);
			} else {
				uploadProcess = ProcessFactory.instance().createUpdateFileProcess(file, networkManager, fileConfiguration);
			}

			File parentFile = file.getParentFile();
			if (sameParents.containsKey(parentFile)) {
				// a sibling exists that already created a sequential process
				SyncProcess process = sameParents.get(parentFile);
				process.add(uploadProcess);
			} else {
				// first file with this parent; create new sequential process
				SyncProcess process = new SyncProcess();
				process.add(uploadProcess);
				sameParents.put(parentFile, process);
			}
		}

		// the children are now grouped together. Next, we need to link the parent files.
		for (File parent : sameParents.keySet()) {
			IProcessComponent<?> asyncChain = new AsyncComponent<>(sameParents.get(parent));
			File parentOfParent = parent.getParentFile();
			if (sameParents.containsKey(parentOfParent)) {
				// parent exists, we add this sub-process (sequential) to it. It can be async here
				SyncProcess process = sameParents.get(parentOfParent);
				process.add(asyncChain);
			} else {
				// parent does not exist --> attach the sub-tree to the root
				rootProcess.add(asyncChain);
			}
		}

		return rootProcess;
	}

	/**
	 * Creates a process chain to delete all files in the list. Note that the list must be in pre-order, else
	 * there will occur errors.
	 * 
	 * @param files list of files to delete in preorder
	 * @param networkManager the network manager with a session
	 * @return the (async) root process component
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	public static AsyncComponent<Void> buildDeletionProcess(List<File> files, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		// the root process
		SyncProcess rootProcess = new SyncProcess();

		// deletion must happen in reverse tree order. Since this is very complicated when it contains
		// asynchronous components, we simply delete them all in the same thread (reverse preorder of course)
		Collections.reverse(files);
		for (File file : files) {
			IProcessComponent<Void> deletionProcess = ProcessFactory.instance()
					.createDeleteFileProcess(file, networkManager);
			rootProcess.add(deletionProcess);
		}

		return new AsyncComponent<Void>(rootProcess);
	}

	/**
	 * This is a workaround to delete files when a {@link FolderIndex} is already existent. Since the node is
	 * already here, the deletion could be speed up because it must not be looked up in the user profile.
	 * 
	 * @param files
	 * @param networkManager
	 * @return
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	@Deprecated
	public static IProcessComponent<Future<Void>> buildDeletionProcessFromNodelist(List<Index> files,
			NetworkManager networkManager) throws NoSessionException, NoPeerConnectionException {
		List<File> filesToDelete = new ArrayList<File>();
		for (Index documentIndex : files) {
			File file = documentIndex.asFile(networkManager.getSession().getRootFile());
			filesToDelete.add(file);
		}

		return buildDeletionProcess(filesToDelete, networkManager);
	}

	/**
	 * Creates a process with all children processes.
	 * 
	 * @param files the files to download (order does not depend)
	 * @param networkManager the connected node (note, it must have a session)
	 * @return the root process component containing all sub-processes (and sub-tasks)
	 * @throws NoSessionException
	 */
	public static IProcessComponent<Void> buildDownloadProcess(List<Index> files, NetworkManager networkManager)
			throws NoPeerConnectionException, NoSessionException {
		// the root process, where everything runs in parallel (only async children are added)
		SyncProcess rootProcess = new SyncProcess();

		// build a flat map of the folders to download (such that O(1) for each lookup)
		Map<FolderIndex, SyncProcess> folderMap = new HashMap<FolderIndex, SyncProcess>();
		Map<FileIndex, AsyncComponent<Void>> fileMap = new HashMap<FileIndex, AsyncComponent<Void>>();

		for (Index file : files) {
			PublicKey fileKey = file.getFilePublicKey();
			IProcessComponent<Void> downloadProcess = ProcessFactory.instance().createDownloadFileProcess(fileKey,
					networkManager);
			if (file.isFolder()) {
				// when a directory, the process may have multiple children, thus we need a sequential process
				SyncProcess folderProcess = new SyncProcess();
				folderProcess.add(downloadProcess);
				folderMap.put((FolderIndex) file, folderProcess);
			} else {
				// when a file, the process can run in parallel with all siblings (done in next step)
				fileMap.put((FileIndex) file, new AsyncComponent<>(downloadProcess));
			}
		}

		// find children with same parents and make them run in parallel
		// idea: iterate through all children and search for parent in other map. If not there, they can be
		// added to the root process anyway
		for (FileIndex file : fileMap.keySet()) {
			AsyncComponent<Void> fileProcess = fileMap.get(file);
			Index parent = file.getParent();
			if (parent == null) {
				// file is in root, thus we can add it to the root process
				rootProcess.add(fileProcess);
			} else if (folderMap.containsKey(parent)) {
				// the parent exists here
				SyncProcess parentProcess = folderMap.get(parent);
				parentProcess.add(fileProcess);
			} else {
				// file is not in root and parent is not here, thus we simply add it to the root process
				rootProcess.add(fileProcess);
			}
		}

		// files and folder are linked. We now link the folders with other folders
		for (FolderIndex folder : folderMap.keySet()) {
			SyncProcess folderProcess = folderMap.get(folder);
			// In addition, we can make this process run asynchronous because it does not affect the siblings
			AsyncComponent<Void> asyncFolderProcess = new AsyncComponent<>(folderProcess);
			Index parent = folder.getParent();
			if (parent == null) {
				// file is in root, thus we can add it to the root process.
				rootProcess.add(asyncFolderProcess);
			} else if (folderMap.containsKey(parent)) {
				// this folder has a parent
				SyncProcess parentProcess = folderMap.get(parent);
				parentProcess.add(asyncFolderProcess);
			} else {
				// folder is not in root and parent folder does not sync here, add it to the root process
				rootProcess.add(asyncFolderProcess);
			}
		}

		return rootProcess;
	}

	/**
	 * Creates a process chain of the given processes in the same order
	 * 
	 * @param processes the processes to align to a center
	 * @return the process chain
	 */
	public static IProcessComponent<Void> createProcessChain(List<IProcessComponent<Void>> processes) {
		SyncProcess rootProcess = new SyncProcess();
		for (IProcessComponent<Void> processComponent : processes) {
			rootProcess.add(processComponent);
		}
		return rootProcess;
	}

	/**
	 * Get a list of all files and subfiles in the root directory. The files are visited and returned in
	 * preorder
	 * 
	 * @param root
	 * @return
	 */
	public static List<File> getPreorderList(File root) {
		List<File> allFiles = new ArrayList<File>();
		listFiles(root, allFiles);
		return allFiles;
	}

	private static void listFiles(File file, List<File> preorderList) {
		preorderList.add(file);
		File[] listFiles = file.listFiles();
		if (listFiles != null) {
			for (File child : listFiles) {
				listFiles(child, preorderList);
			}
		}
	}
}
