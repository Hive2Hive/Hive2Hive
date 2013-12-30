package org.hive2hive.core.process.util;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.process.Process;

/**
 * Additionally holds a {@link File}
 * 
 * @author Nico
 * 
 */
public class FileProcessTreeNode extends ProcessTreeNode {
	private final Path path;

	public FileProcessTreeNode(Process process, ProcessTreeNode parent, Path path) {
		super(process, parent);
		this.path = path;
	}

	public FileProcessTreeNode() {
		super();
		this.path = null;
	}

	public Path getPath() {
		return path;
	}
}
