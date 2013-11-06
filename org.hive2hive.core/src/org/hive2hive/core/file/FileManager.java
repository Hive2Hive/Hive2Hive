package org.hive2hive.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.hive2hive.core.model.FileTreeNode;

public class FileManager {

	public static final String FILE_SEP = System.getProperty("file.separator");
	private final File root;

	public FileManager(String rootDirectory) {
		root = new File(rootDirectory);
	}

	public FileManager(File rootDirectory) {
		root = rootDirectory;
	}

	public File getRoot() {
		return root;
	}

	public File getFile(FileTreeNode fileToFind) throws FileNotFoundException {
		File file = new File(root, fileToFind.getFullPath());
		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		return file;
	}

	public Set<FileTreeNode> getMissingOnDisk(FileTreeNode rootNode) {
		Set<FileTreeNode> missingOnDisk = new HashSet<FileTreeNode>();

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

	public Set<File> getMissingInTree(FileTreeNode rootNode) {
		Set<File> missingInTree = new HashSet<File>();

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
