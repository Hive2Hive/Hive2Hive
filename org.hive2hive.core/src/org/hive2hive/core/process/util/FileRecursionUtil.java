package org.hive2hive.core.process.util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.delete.DeleteFileProcess;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;

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
	 * Creates a process queue based on a list of files. Note that the list must be in
	 * preorder (root first, even if the action is DELETE). Each queue element is a process, when it has
	 * finished, the process starts the next element in the queue.
	 * 
	 * @param files preorder list of files
	 * @param networkManager the network manager
	 * @param action which action to perform (decides over kind of process)
	 * @return the root process which can be started
	 */
	public static Process buildProcessList(List<Path> files, NetworkManager networkManager,
			FileProcessAction action) {
		List<Process> processes = new ArrayList<Process>(files.size());
		for (Path path : files) {
			try {
				// initialize the process and add it to the list
				switch (action) {
					case NEW_FILE:
						processes.add(new NewFileProcess(path.toFile(), networkManager));
						break;
					case MODIFY_FILE:
						processes.add(new NewVersionProcess(path.toFile(), networkManager));
						break;
					case DELETE:
						processes.add(new DeleteFileProcess(path.toFile(), networkManager));
						break;
					default:
						logger.error("Type mismatch");
						return null;
				}
			} catch (IllegalFileLocation e) {
				logger.error("File cannot be uploaded", e);
			} catch (NoSessionException e) {
				logger.error("File cannot be uploaded because there is no session", e);
			}
		}

		if (action == FileProcessAction.DELETE) {
			// deletion happens in reverse order
			Collections.reverse(processes);
		}

		// only start the next process if the previous failed at modify case
		boolean startAtFail = action == FileProcessAction.MODIFY_FILE;

		return new ProcessChain(processes, startAtFail);
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

	/**
	 * Creates a process tree based on a list of files to download from the DHT. Note that the list must be in
	 * preorder.
	 * 
	 * @param toDownload preorder list of files
	 * @param networkManager the network manager
	 * @return the root process which can be started and holds all necessary information of its child
	 *         processes
	 */
	public static ProcessTreeNode buildProcessTreeForDownload(List<FileTreeNode> toDownload,
			NetworkManager networkManager) {
		NodeProcessTreeNode rootProcess = new NodeProcessTreeNode();
		for (FileTreeNode node : toDownload) {
			ProcessTreeNode parent = getParent(rootProcess, node);
			try {
				// initialize the process
				Process process = new DownloadFileProcess(node, networkManager);
				new NodeProcessTreeNode(process, parent, node);
			} catch (NoSessionException e) {
				logger.error("File cannot be downloaded because there is no session");
			}
		}

		return rootProcess;
	}

	/**
	 * Finds the parent process node
	 */
	private static ProcessTreeNode getParent(NodeProcessTreeNode root, FileTreeNode node) {
		FileTreeNode parent = node.getParent();
		for (ProcessTreeNode child : root.getAllChildren()) {
			NodeProcessTreeNode treeNode = (NodeProcessTreeNode) child;
			// skip non-directories
			if (treeNode.getNode().getFullPath().equals(parent.getFullPath())) {
				return treeNode;
			}
		}

		return root;
	}

	/**
	 * Creates a process queue based on a list of files to delete in the DHT. Note that the list must be in
	 * preorder.
	 * 
	 * @param toDelete preorder list of files
	 * @param networkManager the network manager
	 * @return the root process which can be started
	 */
	public static Process buildProcessTreeForDeletion(List<FileTreeNode> toDelete,
			NetworkManager networkManager) {
		// delete the files in the DHT that are deleted while offline. First create a normal queue, but the
		// order must be reverse. With the created queue, reverse it in a 2nd step.
		List<Process> processes = new ArrayList<Process>(toDelete.size());
		for (FileTreeNode node : toDelete) {
			try {
				// initialize the process
				processes.add(new DeleteFileProcess(node, networkManager));
			} catch (IllegalArgumentException e) {
				logger.error("File cannot be deleted", e);
			} catch (NoSessionException e) {
				logger.error("File cannot be deleted because there is no session", e);
			}
		}

		// files are deleted in reverse order (not pre-order)
		// get parent means get the child files --> start deletion at children,
		// thus the list must be reversed (by using the depth)
		Collections.reverse(processes);
		return new ProcessChain(processes, false);
	}
}
