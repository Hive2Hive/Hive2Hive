package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
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
	private final File root;
	private final String configFilePath;

	public PersistenceFileVisitor(File root) {
		this.root = root;
		fileTree = new HashMap<String, byte[]>();

		configFilePath = root.getAbsolutePath() + H2HConstants.META_FILE_NAME;
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		// ignore configFile
		File file = path.toAbsolutePath().toFile();
		if (file.getAbsolutePath().equalsIgnoreCase(configFilePath)) {
			return FileVisitResult.CONTINUE;
		}

		// add to fileTree
		String relativePath = file.getAbsolutePath().replaceFirst(root.getAbsolutePath(), "");
		byte[] md5 = EncryptionUtil.generateMD5Hash(file);
		fileTree.put(relativePath, md5);

		return super.visitFile(path, attrs);
	}

	public HashMap<String, byte[]> getFileTree() {
		return fileTree;
	}
}
