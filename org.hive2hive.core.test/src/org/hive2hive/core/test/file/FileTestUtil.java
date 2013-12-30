package org.hive2hive.core.test.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;

public class FileTestUtil {

	public static File createFileRandomContent(int numOfChunks, FileManager fileManager,
			IH2HFileConfiguration config) throws IOException {
		// create file of size of multiple numbers of chunks
		String random = "";
		while (Math.ceil(1.0 * random.getBytes().length / config.getChunkSize()) < numOfChunks) {
			random += H2HJUnitTest.generateRandomString(config.getChunkSize() - 1);
		}

		String fileName = NetworkTestUtil.randomString();
		File file = new File(fileManager.getRoot().toFile(), fileName);
		FileUtils.write(file, random);

		return file;
	}
}
