package org.hive2hive.core.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Visits all files of a given path and collects the file tree which then can be stored as meta data to disc.
 * 
 * @author Nico
 * 
 */
public class PersistenceFileVisitor extends SimpleFileVisitor<Path> {

	private final HashMap<String, byte[]> fileTree;
	private final Path root;
	private final Path configFilePath;

	public PersistenceFileVisitor(Path root) {
		this.root = root;
		fileTree = new HashMap<String, byte[]>();

		configFilePath = Paths.get(root.toString(), H2HConstants.META_FILE_NAME);
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		// ignore configFile
		if (path.equals(configFilePath)) {
			return FileVisitResult.CONTINUE;
		}

		addToMap(path);
		return super.visitFile(path, attrs);
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// ignore root directory
		if (dir.equals(root)) {
			return FileVisitResult.CONTINUE;
		}

		addToMap(dir);
		return super.preVisitDirectory(dir, attrs);
	}

	private void addToMap(Path path) throws IOException {
		// add to fileTree
		Path relativePath = root.relativize(path);
		
		byte[] md5 = EncryptionUtil.generateMD5Hash(path.toFile());
		fileTree.put(relativePath.toString(), md5);
	}

	public HashMap<String, byte[]> getFileTree() {
		return fileTree;
	}
}
