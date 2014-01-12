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
	 * @return the root process which can be started and holds all necessary information of its child
	 *         processes
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

		// the list is now in the correct order, build the processes now
		Process previous = null;
		for (Process process : processes) {
			if (previous != null) {
				previous.addListener(new StartNextProcessListener(process, previous, startAtFail));
			}

			previous = process;
		}

		// omit NullPointerExceptions
		if (processes.isEmpty()) {
			// return dummy process to omit NPE
			return new Process(null) {
			};
		} else {
			return processes.get(0);
		}
	}

	/**
	 * Finds the parent process node: Either the parent file or the sibling (to omit conflicts when modifying
	 * the parent)
	 * 
	 * @param parentModificationSensitive normally this parameter is false. However, some processes (like
	 *            adding files) modify some data of the parent, thus it can lead to race conditions when two
	 *            sibling are running concurrent. To indicate this, the parameter should be true. Then, the
	 *            previous sibling is returned.
	 */
	private static ProcessTreeNode getParent(FileProcessTreeNode root, Path path,
			boolean parentModificationSensitive) {
		// iterate through all nodes
		for (ProcessTreeNode node : root.getAllChildren()) {
			FileProcessTreeNode fileNode = (FileProcessTreeNode) node;
			// check if the path matches with the parent's path
			if (fileNode.getPath().equals(path.getParent())) {
				if (parentModificationSensitive) {
					return getParentOrSibling(fileNode);
				} else {
					return fileNode;
				}

			}
		}

		return root;
	}

	/**
	 * Returns the previous sibling or the parent itself when no sibling yet
	 */
	private static ProcessTreeNode getParentOrSibling(ProcessTreeNode parent) {
		List<ProcessTreeNode> children = parent.getChildren();
		if (children == null || children.isEmpty()) {
			// no children yet, add the first
			return parent;
		} else {
			// return the last children (by our definition)
			return children.get(children.size() - 1);
		}
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
	 * Creates a process tree based on a list of files to delete in the DHT. Note that the list must be in
	 * preorder.
	 * 
	 * @param toDelete preorder list of files
	 * @param networkManager the network manager
	 * @return the root process which can be started and holds all necessary information of its child
	 *         processes
	 */
	public static ProcessTreeNode buildProcessTreeForDeletion(List<FileTreeNode> toDelete,
			NetworkManager networkManager) {
		// delete the files in the DHT that are deleted while offline. First create a normal tree, but the
		// order must be reverse. With the created tree, reverse it in a 2nd step.
		NodeProcessTreeNode rootProcess = new NodeProcessTreeNode();
		for (FileTreeNode node : toDelete) {
			ProcessTreeNode parent = getParent(rootProcess, node);
			try {
				// initialize the process
				DeleteFileProcess deleteProcess = new DeleteFileProcess(node, networkManager);
				new NodeProcessTreeNode(deleteProcess, parent, node);
			} catch (IllegalArgumentException e) {
				logger.error("File cannot be deleted", e);
			} catch (NoSessionException e) {
				logger.error("File cannot be deleted because there is no session", e);
			}
		}

		// files are deleted in reverse order (not pre-order)
		// get parent means get the child files --> start deletion at children,
		// thus the tree must be reversed (by using the depth)
		return reverseTree(rootProcess);
	}

	/**
	 * Reverse a tree (preserving the levels, but swapping parent-child relations).
	 */
	private static ProcessTreeNode reverseTree(ProcessTreeNode root) {
		List<ProcessTreeNode> allNodes = root.getAllChildren();
		NodeProcessTreeNode reverseRootProcess = new NodeProcessTreeNode();
		ProcessTreeNode currentParent = reverseRootProcess;
		int currentDepth = allNodes.size();
		while (currentDepth >= 0) {
			for (ProcessTreeNode processNode : allNodes) {
				if (processNode.getDepth() == currentDepth) {
					currentParent.addChild(processNode);
				}
			}

			if (currentParent.getChildren() != null && !currentParent.getChildren().isEmpty()) {
				// children are filled --> take first child as new parent
				currentParent = currentParent.getChildren().get(0);
			}

			// decrease depth
			currentDepth--;
		}

		return reverseRootProcess;
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
}
