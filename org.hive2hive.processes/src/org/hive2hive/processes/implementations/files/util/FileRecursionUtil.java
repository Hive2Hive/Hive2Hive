package org.hive2hive.processes.implementations.files.util;

import java.io.File;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.ProcessFactory;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.decorators.AsyncComponent;

public class FileRecursionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileRecursionUtil.class);

	private FileRecursionUtil() {
		// only static methods
	}

	public enum FileProcessAction {
		NEW_FILE,
		MODIFY_FILE,
		DELETE;
	}

	/**
	 * Creates a process with all children processes.
	 * 
	 * @param files the files to download (order does not depend)
	 * @param networkManager the connected node (note, it must have a session)
	 * @return the root process component containing all sub-processes (and sub-tasks)
	 */
	public static ProcessComponent buildDownloadProcess(List<FileTreeNode> files,
			NetworkManager networkManager) {
		// the root process, where everything runs in parallel
		SequentialProcess rootProcess = new SequentialProcess();

		// build a flat map of the folders to download (such that O(1) for each lookup)
		Map<FileTreeNode, SequentialProcess> folderMap = new HashMap<FileTreeNode, SequentialProcess>();
		Map<FileTreeNode, AsyncComponent> fileMap = new HashMap<FileTreeNode, AsyncComponent>();

		for (FileTreeNode file : files) {
			PublicKey fileKey = file.getKeyPair().getPublic();
			ProcessComponent downloadProcess = ProcessFactory.instance().createDownloadFileProcess(fileKey,
					networkManager);
			if (file.isFolder()) {
				// when a directory, the process may have multiple children, thus we need a sequential process
				SequentialProcess folderProcess = new SequentialProcess();
				folderProcess.add(downloadProcess);
				folderMap.put(file, folderProcess);
			} else {
				// when a file, the process can run in parallel with all siblings (done in next step)
				fileMap.put(file, new AsyncComponent(downloadProcess));
			}
		}

		// find children with same parents and make them run in parallel
		// idea: iterate through all children and search for parent in other map. If not there, they can be
		// added to the root process anyway
		for (FileTreeNode file : fileMap.keySet()) {
			AsyncComponent fileProcess = fileMap.get(file);
			FileTreeNode parent = file.getParent();
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
		for (FileTreeNode folder : folderMap.keySet()) {
			SequentialProcess folderProcess = folderMap.get(folder);
			// In addition, we can make this process run asynchronous because it does not affect the siblings
			AsyncComponent asyncFolderProcess = new AsyncComponent(folderProcess);
			FileTreeNode parent = folder.getParent();
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
		if (listFiles != null)
			for (File child : listFiles) {
				listFiles(child.toPath(), preorderList);
			}
	}
}
