package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.security.EncryptionUtil;

public class FileManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileManager.class);
	public static final String FILE_SEP = System.getProperty("file.separator");
	private final File root;

	// holds persistent meta data
	private final File h2hMetaFile;

	public FileManager(String rootDirectory) {
		this(new File(rootDirectory));
	}

	public FileManager(File rootDirectory) {
		root = rootDirectory;
		if (!root.exists()) {
			root.mkdirs();
		}

		h2hMetaFile = new File(root, H2HConstants.META_FILE_NAME);
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
	 * Writes the meta data (used to synchronize) to the disk
	 */
	public void writePersistentMetaData() {
		// generate the new persistent meta data
		PersistentMetaData metaData = new PersistentMetaData();
		try {
			PersistenceFileVisitor visitor = new PersistenceFileVisitor(root);
			Files.walkFileTree(root.toPath(), visitor);
			metaData.setFileTree(visitor.getFileTree());

			byte[] encoded = EncryptionUtil.serializeObject(metaData);
			FileUtils.writeByteArrayToFile(h2hMetaFile, encoded);
		} catch (IOException e) {
			logger.error("Cannot write the meta data", e);
		}
	}

	/**
	 * Reads the meta data (used to synchronize) from the disk
	 * 
	 * @return the read meta data (never null)
	 */
	public PersistentMetaData getPersistentMetaData() {
		try {
			byte[] content = FileUtils.readFileToByteArray(h2hMetaFile);
			PersistentMetaData metaData = (PersistentMetaData) EncryptionUtil.deserializeObject(content);
			return metaData;
		} catch (IOException e) {
			logger.error("Cannot read the last meta data");
			return new PersistentMetaData();
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
}
