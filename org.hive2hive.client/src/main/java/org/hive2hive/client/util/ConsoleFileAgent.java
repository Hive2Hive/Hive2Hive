package org.hive2hive.client.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.IFileAgent;

public class ConsoleFileAgent implements IFileAgent {

	private final File root;
	private final File cache;

	public ConsoleFileAgent(File root) {
		this.root = root;
		this.cache = new File(FileUtils.getTempDirectory(), "H2H-Cache");
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	public void writeCache(String key, byte[] data) throws IOException {
		FileUtils.writeByteArrayToFile(new File(cache, key), data);
	}

	@Override
	public byte[] readCache(String key) throws IOException {
		return FileUtils.readFileToByteArray(new File(cache, key));
	}
}
