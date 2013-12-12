package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileUtil {

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
}
