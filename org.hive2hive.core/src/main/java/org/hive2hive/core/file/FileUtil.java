package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.IH2HSerialize;
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
	public static void writePersistentMetaData(IFileAgent fileAgent, PublicKeyManager keyManager,
			DownloadManager downloadManager, IH2HSerialize serializer) throws IOException {
		// generate the new persistent meta data
		PersistentMetaData metaData = new PersistentMetaData();

		// add the public keys
		if (keyManager != null) {
			metaData.setPublicKeyCache(keyManager.getCachedPublicKeys());
		}

		if (downloadManager != null) {
			metaData.setDownloads(downloadManager.getOpenTasks());
		}

		byte[] encoded = serializer.serialize(metaData);
		fileAgent.writeCache(H2HConstants.META_FILE_NAME, encoded);
	}

	/**
	 * Reads the meta data (used to synchronize) from the disk
	 * 
	 * @return the read meta data (never null)
	 */
	public static PersistentMetaData readPersistentMetaData(IFileAgent fileAgent, IH2HSerialize serializer) {
		try {
			byte[] content = fileAgent.readCache(H2HConstants.META_FILE_NAME);
			if (content == null || content.length == 0) {
				logger.warn("Not found the meta data. Create new one");
				return new PersistentMetaData();
			}
			return (PersistentMetaData) serializer.deserialize(content);
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Cannot deserialize meta data. Reason: {}", e.getMessage());
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
	 * Makes a file path relative to the base
	 */
	public static File relativize(File base, File child) {
		return new File(base.toURI().relativize(child.toURI()).getPath());
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
	 * Checks whether the given file is in the given H2H root folder (note, the user must be logged in).
	 * 
	 * @param file the file to test
	 * @param session a valid session of any user
	 * @return true when the file is within the H2H directory, otherwise false
	 */
	public static boolean isInH2HDirectory(File file, File root) {
		if (root == null || file == null) {
			return false;
		}

		return file.getAbsolutePath().toString().startsWith(root.getAbsolutePath());
	}

	/**
	 * Does the same as {@link #isInH2HDirectory(IFileAgent, File, File)} but taking a session as parameter
	 */
	public static boolean isInH2HDirectory(IFileAgent fileAgent, File file) {
		return fileAgent == null ? false : isInH2HDirectory(file, fileAgent.getRoot());
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
