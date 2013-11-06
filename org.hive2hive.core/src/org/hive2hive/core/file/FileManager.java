package org.hive2hive.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
}
