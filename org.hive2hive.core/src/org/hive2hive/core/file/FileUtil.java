package org.hive2hive.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.IndexNode;

public class FileUtil {

	private final static Logger logger = H2HLoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
		// only static methods
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
			return file.length();
		} finally {
			try {
				if (stream != null)
					stream.close();
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
	 * @param oldParent the old parent {@link IndexNode}
	 * @param newParent the new parent {@link IndexNode}
	 * @param fileManager the {@link FileManager} of the user
	 * @throws IOException when moving went wrong
	 */
	public static void moveFile(String sourceName, String destName, IndexNode oldParent,
			IndexNode newParent, FileManager fileManager) throws IOException {
		// find the file of this user on the disc
		File oldParentFile = fileManager.getPath(oldParent).toFile();
		File toMoveSource = new File(oldParentFile, sourceName);

		if (!toMoveSource.exists()) {
			throw new FileNotFoundException("Cannot move file '" + toMoveSource.getAbsolutePath()
					+ "' because it's not at the source location anymore");
		}

		File newParentFile = fileManager.getPath(newParent).toFile();
		File toMoveDest = new File(newParentFile, destName);

		if (toMoveDest.exists()) {
			logger.warn("Overwriting '" + toMoveDest.getAbsolutePath()
					+ "' because file has been moved remotely");
		}

		// move the file
		Files.move(toMoveSource.toPath(), toMoveDest.toPath(), StandardCopyOption.ATOMIC_MOVE);
		logger.debug("Successfully moved the file from " + toMoveSource.getAbsolutePath() + " to "
				+ toMoveDest.getAbsolutePath());
	}
}
