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
import org.hive2hive.core.serializer.IH2HSerialize;
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
	 * @param fileAgent the file agent
	 * @param keyManager the key manager
	 * @param serializer the serializer to use
	 * @throws IOException if the data cannot be serialized or stored
	 */
	public static void writePersistentMetaData(org.hive2hive.core.file.IFileAgent fileAgent, PublicKeyManager keyManager, IH2HSerialize serializer)
			throws IOException {
		// generate the new persistent meta data
		org.hive2hive.core.file.PersistentMetaData metaData = new org.hive2hive.core.file.PersistentMetaData();

		// add the public keys
		if (keyManager != null) {
			metaData.setPublicKeyCache(keyManager.getCachedPublicKeys());
		}

		byte[] encoded = serializer.serialize(metaData);
		fileAgent.writeCache(H2HConstants.META_FILE_NAME, encoded);
	}

	/**
	 * Reads the meta data (used to synchronize) from the disk
	 * 
	 * @param fileAgent the file agent
	 * @param serializer the serializer to use
	 * @return the read meta data (never null)
	 */
	public static org.hive2hive.core.file.PersistentMetaData readPersistentMetaData(org.hive2hive.core.file.IFileAgent fileAgent, IH2HSerialize serializer) {
		try {
			byte[] content = fileAgent.readCache(H2HConstants.META_FILE_NAME);
			if (content == null || content.length == 0) {
				logger.warn("Not found the meta data. Create new one");
				return new org.hive2hive.core.file.PersistentMetaData();
			}
			return (org.hive2hive.core.file.PersistentMetaData) serializer.deserialize(content);
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Cannot deserialize meta data. Reason: {}", e.getMessage());
			return new org.hive2hive.core.file.PersistentMetaData();
		}
	}

	/**
	 * Returns the file separator of the operating system
	 * 
	 * @return the file separator of the current operating system
	 */
	public static String getFileSep() {
		return System.getProperty("file.separator");
	}

	/**
	 * Makes a file path relative to the base
	 * 
	 * @param base the base file
	 * @param child the child file
	 * @return the relative file
	 */
	public static File relativize(File base, File child) throws SecurityException{
		return new File(base.toURI().relativize(child.toURI()).getPath());
	}

	/**
	 * Note that file.length can be very slowly (see
	 * http://stackoverflow.com/questions/116574/java-get-file-size-efficiently)
	 * 
	 * @param file the file to determine the size
	 * @return the file size in bytes
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
	 * @param root the current root
	 * @return true when the file is within the H2H directory, otherwise false
	 */
	public static boolean isInH2HDirectory(File file, File root) {
		if (root == null || file == null) {
			return false;
		}

		return file.getAbsolutePath().toString().startsWith(root.getAbsolutePath());
	}

	/**
	 * Does the same as {@link #isInH2HDirectory(File, File)} but taking a session as parameter
	 * 
	 * @param fileAgent the file agent
	 * @param file the file to check
	 * @return whether the file is in the managed directory
	 */
	public static boolean isInH2HDirectory(org.hive2hive.core.file.IFileAgent fileAgent, File file) {
		return fileAgent == null ? false : isInH2HDirectory(file, fileAgent.getRoot());
	}

	/**
	 * Sorts the given list in pre-order style.
	 * 
	 * @param unsortedFiles a list of unsorted file. The list will be updated.
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
