package org.hive2hive.core.process.util;

import java.io.File;
import java.util.ArrayList;
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
		MODIFY_FILE;
	}

	/**
	 * Creates a process tree based on a list of files to upload to the DHT. Note that the list must be in
	 * preorder.
	 * 
	 * @param toUpload preorder list of files
	 * @param networkManager the network manager
	 * @param action which action to perform (decides over kind of process)
	 * @return the root process which can be started and holds all necessary information of its child
	 *         processes
	 */
	public static ProcessTreeNode buildProcessTreeUpload(List<File> toUpload, NetworkManager networkManager,
			FileProcessAction action) {
		// synchronize the files that need to be uploaded into the DHT
		FileProcessTreeNode rootProcess = new FileProcessTreeNode();
		for (File file : toUpload) {
			ProcessTreeNode parent = getParent(rootProcess, file);
			try {
				// initialize the process
				Process process = null;
				switch (action) {
					case NEW_FILE:
						process = new NewFileProcess(file, networkManager);
						break;
					case MODIFY_FILE:
						process = new NewVersionProcess(file, networkManager);
						break;
					default:
						logger.error("Type mismatch");
						continue;
				}

				new FileProcessTreeNode(process, parent, file);
			} catch (IllegalFileLocation e) {
				logger.error("File cannot be uploaded", e);
			} catch (NoSessionException e) {
				logger.error("File cannot be uploaded because there is no session", e);
			}
		}

		return rootProcess;
	}

	/**
	 * Finds the parent process node
	 */
	private static ProcessTreeNode getParent(FileProcessTreeNode root, File file) {
		ProcessTreeNode current = root;
		for (ProcessTreeNode child : root.getChildren()) {
			FileProcessTreeNode childProcess = (FileProcessTreeNode) child;
			File childNode = childProcess.getFile();
			if (childNode.isDirectory()) {
				// skip non-directories
				if (file.getAbsolutePath().startsWith(childNode.getAbsolutePath())) {
					current = child;
				}
			}
		}
		return current;
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
		List<ProcessTreeNode> allNodes = new ArrayList<ProcessTreeNode>();
		NodeProcessTreeNode rootProcess = new NodeProcessTreeNode();
		for (FileTreeNode node : toDelete) {
			ProcessTreeNode parent = getParent(rootProcess, node);
			try {
				// initialize the process
				DeleteFileProcess deleteProcess = new DeleteFileProcess(node, networkManager);
				allNodes.add(new NodeProcessTreeNode(deleteProcess, parent, node));
			} catch (IllegalArgumentException e) {
				logger.error("File cannot be deleted", e);
			} catch (NoSessionException e) {
				logger.error("File cannot be deleted because there is no session", e);
			}
		}

		// remove the rootProcess from the process manager because it will never be started
		// TODO not used anymore
		// ProcessManager.getInstance().detachProcess(rootProcess);

		// files are deleted in reverse order (not pre-order)
		// get parent means get the child files --> start deletion at children,
		// thus the tree must be reversed (by using the depth)
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
		ProcessTreeNode current = root;
		for (ProcessTreeNode child : root.getChildren()) {
			NodeProcessTreeNode childProcess = (NodeProcessTreeNode) child;
			FileTreeNode childNode = childProcess.getNode();
			if (childNode.isFolder()) {
				// skip non-directories
				if (node.getFullPath().startsWith(childNode.getFullPath())) {
					current = child;
				}
			}
		}
		return current;
	}
}
