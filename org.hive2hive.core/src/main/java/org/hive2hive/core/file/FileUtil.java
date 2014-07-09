package org.hive2hive.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
		// only static methods
	}

	/**
	 * Writes the meta data (used to synchronize) to the disk
	 * 
	 * @throws IOException
	 */
	public static void writePersistentMetaData(Path root, PublicKeyManager keyManager, DownloadManager downloadManager)
			throws IOException {
		assert root != null;

		// generate the new persistent meta data
		PersistentMetaData metaData = new PersistentMetaData();

		// add the files
		if (root != null) {
			PersistenceFileVisitor visitor = new PersistenceFileVisitor(root);
			Files.walkFileTree(root, visitor);
			metaData.setFileTree(visitor.getFileTree());
		}

		// add the public keys
		if (keyManager != null) {
			metaData.setPublicKeyCache(keyManager.getCachedPublicKeys());
		}

		if (downloadManager != null) {
			metaData.setDownloads(downloadManager.getOpenTasks());
		}

		byte[] encoded = SerializationUtil.serialize(metaData);
		FileUtils.writeByteArrayToFile(Paths.get(root.toString(), H2HConstants.META_FILE_NAME).toFile(), encoded);
	}

	/**
	 * Reads the meta data (used to synchronize) from the disk
	 * 
	 * @return the read meta data (never null)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static PersistentMetaData readPersistentMetaData(Path root) {
		try {
			byte[] content = FileUtils.readFileToByteArray(Paths.get(root.toString(), H2HConstants.META_FILE_NAME).toFile());
			return (PersistentMetaData) SerializationUtil.deserialize(content);
		} catch (IOException | ClassNotFoundException e) {
			logger.warn("Cannot read the persistent meta data. It probably does not exist yet");
			return new PersistentMetaData();
		}
	}

	/**
	 * Returns the file separator of the operating system
	 * 
	 * @return
	 */
	public static String getFileSep() {
		String fileSep = System.getProperty("file.separator");
		if ("\\".equals(fileSep)) {
			fileSep = "\\\\";
		}
		return fileSep;
	}

	/**
	 * Returns the file on disk from a file node of the user profile
	 * 
	 * @param fileToFind
	 * @return the path to the file or null if the parameter is null
	 */
	public static Path getPath(Path root, Index fileToFind) {
		if (fileToFind == null) {
			return null;
		}
		return Paths.get(root.toString(), fileToFind.getFullPath().toString());
	}

	/**
	 * Note that file.length can be very slowly (see
	 * http://stackoverflow.com/questions/116574/java-get-file-size-efficiently)
	 * 
	 * @return the file size in bytes
	 * @throws IOException
	 */
	public static long getFileSize(File file) {
		InputStream stream = null;
		try {
			URL url = file.toURI().toURL();
			stream = url.openStream();
			return stream.available();
		} catch (IOException e) {
			// just make it the traditional way
			return file.length();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Move a file according to their nodes. This operation also support renaming and moving in the same step.
	 * 
	 * @param sourceName the name of the file at the source
	 * @param destName the name of the file at the destination
	 * @param oldParent the old parent {@link FolderIndex}
	 * @param newParent the new parent {@link FolderIndex}
	 * @param fileManager the {@link FileManager} of the user
	 * @throws IOException when moving went wrong
	 */
	public static void moveFile(Path root, String sourceName, String destName, Index oldParent, Index newParent)
			throws IOException {
		// find the file of this user on the disc
		File oldParentFile = FileUtil.getPath(root, oldParent).toFile();
		File toMoveSource = new File(oldParentFile, sourceName);

		if (!toMoveSource.exists()) {
			throw new FileNotFoundException("Cannot move file '" + toMoveSource.getAbsolutePath()
					+ "' because it's not at the source location anymore");
		}

		File newParentFile = FileUtil.getPath(root, newParent).toFile();
		File toMoveDest = new File(newParentFile, destName);

		if (toMoveDest.exists()) {
			logger.warn("Overwriting '{}' because file has been moved remotely.", toMoveDest.getAbsolutePath());
		}

		// move the file
		Files.move(toMoveSource.toPath(), toMoveDest.toPath(), StandardCopyOption.ATOMIC_MOVE);
		logger.debug("Successfully moved the file from {} to {}.", toMoveSource.getAbsolutePath(),
				toMoveDest.getAbsolutePath());
	}

	/**
	 * Checks whether the given file is in the given H2H root folder (note, the user must be logged in).
	 * 
	 * @param file the file to test
	 * @param session a valid session of any user
	 * @return true when the file is within the H2H directory, otherwise false
	 */
	public static boolean isInH2HDirectory(File file, H2HSession session) {
		if (session == null || file == null) {
			return false;
		}

		return file.getAbsolutePath().toString().startsWith(session.getRootFile().getAbsolutePath());
	}

	/**
	 * Sorts the given list in pre-order style
	 * 
	 * @param unsortedFiles
	 */
	public static void sortPreorder(List<File> unsortedFiles) {
		Collections.sort(unsortedFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
	}
}
