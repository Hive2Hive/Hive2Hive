package org.hive2hive.core.test.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;

public class FileTestUtil {

	public static File createFileRandomContent(int numOfChunks, File parent, IFileConfiguration config)
			throws IOException {
		return createFileRandomContent(NetworkTestUtil.randomString(), numOfChunks, parent, config);
	}

	public static File createFileRandomContent(String fileName, int numOfChunks, File parent,
			IFileConfiguration config) throws IOException {
		// create file of size of multiple numbers of chunks
		File file = new File(parent, fileName);
		while (Math.ceil(1.0 * FileUtil.getFileSize(file) / config.getChunkSize()) < numOfChunks) {
			String random = H2HJUnitTest.generateRandomString((int) config.getChunkSize() - 1);
			FileUtils.write(file, random, true);
		}

		return file;
	}

	public static File getTempDirectory() {
		return new File(FileUtils.getTempDirectory(), NetworkTestUtil.randomString());
	}
}
