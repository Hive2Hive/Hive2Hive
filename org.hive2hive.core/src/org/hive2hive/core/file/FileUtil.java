package org.hive2hive.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
		try {
			FileInputStream stream = new FileInputStream(file);
			long size = stream.getChannel().size();
			stream.close();
			return size;
		} catch (IOException e) {
			return file.length();
		}
	}
}
