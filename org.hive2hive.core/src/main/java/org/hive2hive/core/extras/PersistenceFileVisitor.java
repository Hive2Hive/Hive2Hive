package org.hive2hive.core.extras;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.security.HashUtil;

/**
 * Visits all files of a given path and collects the file tree which then can be stored as meta data to disc.
 * This class can be used to create the map for using it with the {@link FileSynchronizer}.
 * 
 * @author Nico
 * 
 */
@Extra
public class PersistenceFileVisitor extends SimpleFileVisitor<Path> {

	private final Map<String, byte[]> fileTree;
	private final File configFilePath;
	private final File root;

	public PersistenceFileVisitor(File root) {
		this.root = root;
		this.configFilePath = new File(root, H2HConstants.META_FILE_NAME);
		this.fileTree = new TreeMap<String, byte[]>();
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		// ignore configFile
		if (path.equals(configFilePath.toPath())) {
			return FileVisitResult.CONTINUE;
		}

		addToMap(path.toFile());
		return super.visitFile(path, attrs);
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// ignore root directory
		if (dir.equals(root)) {
			return FileVisitResult.CONTINUE;
		}

		addToMap(dir.toFile());
		return super.preVisitDirectory(dir, attrs);
	}

	private void addToMap(File file) throws IOException {
		// add to fileTree
		File relative = FileUtil.relativize(root, file);

		byte[] hash = HashUtil.hash(file);
		fileTree.put(relative.toString(), hash);
	}

	public Map<String, byte[]> getFileTree() {
		return fileTree;
	}
}
