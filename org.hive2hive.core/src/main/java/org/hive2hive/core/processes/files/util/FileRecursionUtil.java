package org.hive2hive.core.processes.files.util;

import java.io.File;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.interfaces.IProcessComponent;

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
	public static ProcessComponent buildUploadProcess(List<Path> files, FileProcessAction action,
			NetworkManager networkManager) throws NoSessionException, NoPeerConnectionException {
		// the sequential root process
		SequentialProcess rootProcess = new SequentialProcess();

		// key idea: Find the children with the same parents and add them to a sequential process. They need
		// to be sequential because the parent meta file must be adapted. If they would run in parallel, they
		// would modify the parent meta folder simultaneously.
		Map<Path, SequentialProcess> sameParents = new HashMap<Path, SequentialProcess>();
		for (Path file : files) {
			// create the process which uploads or updates the file
			ProcessComponent uploadProcess;
			if (action == FileProcessAction.NEW_FILE) {
				uploadProcess = ProcessFactory.instance().createNewFileProcess(file.toFile(), networkManager);
			} else {
				uploadProcess = ProcessFactory.instance().createUpdateFileProcess(file.toFile(), networkManager);
			}

			Path parentFile = file.getParent();
			if (sameParents.containsKey(parentFile)) {
				// a sibling exists that already created a sequential process
				SequentialProcess sequentialProcess = sameParents.get(parentFile);
				sequentialProcess.add(uploadProcess);
			} else {
				// first file with this parent; create new sequential process
				SequentialProcess sequentialProcess = new SequentialProcess();
				sequentialProcess.add(uploadProcess);
				sameParents.put(parentFile, sequentialProcess);
			}
		}

		// the children are now grouped together. Next, we need to link the parent files.
		for (Path parent : sameParents.keySet()) {
			AsyncComponent asyncChain = new AsyncComponent(sameParents.get(parent));
			Path parentOfParent = parent.getParent();
			if (sameParents.containsKey(parentOfParent)) {
				// parent exists, we add this sub-process (sequential) to it. It can be async here
				SequentialProcess sequentialProcess = sameParents.get(parentOfParent);
				sequentialProcess.add(asyncChain);
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
	public static ProcessComponent buildDeletionProcess(List<Path> files, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		// the sequential root process
		SequentialProcess rootProcess = new SequentialProcess();

		// deletion must happen in reverse tree order. Since this is very complicated when it contains
		// asynchronous components, we simply delete them all in the same thread (reverse preorder of course)
		Collections.reverse(files);
		for (Path file : files) {
			ProcessComponent deletionProcess = ProcessFactory.instance().createDeleteFileProcess(file.toFile(),
					networkManager);
			rootProcess.add(deletionProcess);
		}

		return new AsyncComponent(rootProcess);
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
	public static ProcessComponent buildDeletionProcessFromNodelist(List<Index> files, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		List<Path> filesToDelete = new ArrayList<Path>();
		for (Index documentIndex : files) {
			filesToDelete.add(FileUtil.getPath(networkManager.getSession().getRoot(), documentIndex));
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
	public static ProcessComponent buildDownloadProcess(List<Index> files, NetworkManager networkManager)
			throws NoSessionException {
		// the root process, where everything runs in parallel (only async children are added)
		SequentialProcess rootProcess = new SequentialProcess();

		// build a flat map of the folders to download (such that O(1) for each lookup)
		Map<FolderIndex, SequentialProcess> folderMap = new HashMap<FolderIndex, SequentialProcess>();
		Map<FileIndex, AsyncComponent> fileMap = new HashMap<FileIndex, AsyncComponent>();

		for (Index file : files) {
			PublicKey fileKey = file.getFilePublicKey();
			ProcessComponent downloadProcess = ProcessFactory.instance().createDownloadFileProcess(fileKey, networkManager);
			if (file.isFolder()) {
				// when a directory, the process may have multiple children, thus we need a sequential process
				SequentialProcess folderProcess = new SequentialProcess();
				folderProcess.add(downloadProcess);
				folderMap.put((FolderIndex) file, folderProcess);
			} else {
				// when a file, the process can run in parallel with all siblings (done in next step)
				fileMap.put((FileIndex) file, new AsyncComponent(downloadProcess));
			}
		}

		// find children with same parents and make them run in parallel
		// idea: iterate through all children and search for parent in other map. If not there, they can be
		// added to the root process anyway
		for (FileIndex file : fileMap.keySet()) {
			AsyncComponent fileProcess = fileMap.get(file);
			Index parent = file.getParent();
			if (parent == null) {
				// file is in root, thus we can add it to the root process
				rootProcess.add(fileProcess);
			} else if (folderMap.containsKey(parent)) {
				// the parent exists here
				SequentialProcess parentProcess = folderMap.get(parent);
				parentProcess.add(fileProcess);
			} else {
				// file is not in root and parent is not here, thus we simply add it to the root process
				rootProcess.add(fileProcess);
			}
		}

		// files and folder are linked. We now link the folders with other folders
		for (FolderIndex folder : folderMap.keySet()) {
			SequentialProcess folderProcess = folderMap.get(folder);
			// In addition, we can make this process run asynchronous because it does not affect the siblings
			AsyncComponent asyncFolderProcess = new AsyncComponent(folderProcess);
			Index parent = folder.getParent();
			if (parent == null) {
				// file is in root, thus we can add it to the root process.
				rootProcess.add(asyncFolderProcess);
			} else if (folderMap.containsKey(parent)) {
				// this folder has a parent
				SequentialProcess parentProcess = folderMap.get(parent);
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
	public static IProcessComponent createProcessChain(List<ProcessComponent> processes) {
		SequentialProcess rootProcess = new SequentialProcess();
		for (ProcessComponent processComponent : processes) {
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
	public static List<Path> getPreorderList(Path root) {
		List<Path> allFiles = new ArrayList<Path>();
		listFiles(root, allFiles);
		return allFiles;
	}

	private static void listFiles(Path path, List<Path> preorderList) {
		preorderList.add(path);
		File[] listFiles = path.toFile().listFiles();
		if (listFiles != null) {
			for (File child : listFiles) {
				listFiles(child.toPath(), preorderList);
			}
		}
	}
}
