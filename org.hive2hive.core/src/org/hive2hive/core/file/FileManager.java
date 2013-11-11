package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.hive2hive.core.model.FileTreeNode;

public class FileManager {

	public static final String FILE_SEP = System.getProperty("file.separator");
	private final File root;

	public FileManager(String rootDirectory) {
		this(new File(rootDirectory));
	}

	public FileManager(File rootDirectory) {
		root = rootDirectory;
		if (!root.exists()) {
			root.mkdirs();
		}
	}

	/**
	 * Returns the root node
	 * 
	 * @return
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * Creates a file on disk for the given node
	 * 
	 * @param fileToCreate
	 * @return whether the file creation was successful.
	 * @throws IOException Can occur if the directory cannot be written
	 */
	public boolean createFileOnDisk(FileTreeNode fileToCreate) throws IOException {
		File file = getFile(fileToCreate);
		if (file.exists()) {
			throw new FileAlreadyExistsException(fileToCreate.getFullPath());
		}

		if (fileToCreate.isFolder()) {
			return file.mkdir();
		} else {
			return file.createNewFile();
		}
	}

	/**
	 * Returns the file on disk from a file node of the user profile
	 * 
	 * @param fileToFind
	 * @return
	 */
	public File getFile(FileTreeNode fileToFind) {
		String fullPath = root.getAbsolutePath() + fileToFind.getFullPath();
		return new File(fullPath);
	}

	/**
	 * Returns a list of nodes of the file tree that are nowhere on the disk. The list is in pre-order
	 * 
	 * @param rootNode
	 * @return
	 */
	public List<FileTreeNode> getMissingOnDisk(FileTreeNode rootNode) {
		List<FileTreeNode> missingOnDisk = new ArrayList<FileTreeNode>();

		// visit all files in the tree and compare to disk
		Stack<FileTreeNode> fileStack = new Stack<FileTreeNode>();
		fileStack.push(rootNode);
		while (!fileStack.isEmpty()) {
			FileTreeNode top = fileStack.pop();
			File topFile = new File(root, top.getFullPath());
			if (!topFile.exists()) {
				missingOnDisk.add(top);
			}

			// add children to stack
			for (FileTreeNode child : top.getChildren()) {
				fileStack.push(child);
			}
		}

		return missingOnDisk;
	}

	/**
	 * Returns the missing files that exist on disk but not in the file tree in the user profile. The list is
	 * in pre-order
	 * 
	 * @param rootNode
	 * @return
	 */
	public List<File> getMissingInTree(FileTreeNode rootNode) {
		List<File> missingInTree = new ArrayList<File>();

		Stack<File> fileStack = new Stack<File>();
		for (File file : root.listFiles()) {
			fileStack.push(file);
		}

		while (!fileStack.isEmpty()) {
			File top = fileStack.pop();
			String relativePath = top.getAbsolutePath().replace(root.getAbsolutePath(), "")
					.replaceFirst("/", "");

			// split by the file separator and navigate in the tree
			String[] split = relativePath.split(FILE_SEP);
			FileTreeNode current = rootNode;
			for (int i = 0; i < split.length; i++) {
				FileTreeNode child = current.getChildByName(split[i]);
				if (child == null) {
					missingInTree.add(top);
					break;
				} else {
					current = child;
				}
			}

			if (top.isDirectory()) {
				for (File file : top.listFiles()) {
					fileStack.push(file);
				}
			}
		}

		return missingInTree;
	}
}
