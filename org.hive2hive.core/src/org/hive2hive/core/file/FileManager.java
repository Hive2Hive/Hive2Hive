package org.hive2hive.core.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.security.EncryptionUtil;

public class FileManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileManager.class);
	private final Path root;

	public static String getFileSep() {
		String fileSep = System.getProperty("file.separator");
		if (fileSep.equals("\\"))
			fileSep = "\\\\";
		return fileSep;
	}

	// holds persistent meta data
	private final Path h2hMetaFile;

	public FileManager(String rootDirectory) {
		this(Paths.get(rootDirectory));
	}

	public FileManager(Path rootDirectory) {
		root = rootDirectory;
		if (!root.toFile().exists()) {
			root.toFile().mkdirs();
		}

		h2hMetaFile = Paths.get(root.toString(), H2HConstants.META_FILE_NAME);
	}

	/**
	 * Returns the root node
	 * 
	 * @return
	 */
	public Path getRoot() {
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
			Files.walkFileTree(root, visitor);
			metaData.setFileTree(visitor.getFileTree());

			byte[] encoded = EncryptionUtil.serializeObject(metaData);
			FileUtils.writeByteArrayToFile(h2hMetaFile.toFile(), encoded);
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
			byte[] content = FileUtils.readFileToByteArray(h2hMetaFile.toFile());
			PersistentMetaData metaData = (PersistentMetaData) EncryptionUtil.deserializeObject(content);
			return metaData;
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Cannot read the last meta data");
			return new PersistentMetaData();
		}
	}

	/**
	 * Returns the file on disk from a file node of the user profile
	 * 
	 * @param fileToFind
	 * @return the path to the file or null if the parameter is null
	 */
	public Path getPath(Index fileToFind) {
		if (fileToFind == null)
			return null;
		return Paths.get(root.toString(), fileToFind.getFullPath().toString());
	}
}
